#!/usr/bin/env tsx

import ts from "typescript";
import fs from "node:fs";
import path from "node:path";
import { parseTemplate } from "@angular/compiler";
import {
  AST,
  Call as NgCall,
  PropertyRead,
  SafePropertyRead,
  RecursiveAstVisitor,
  TmplAstNode,
  TmplAstBoundAttribute,
  TmplAstBoundEvent,
  TmplAstBoundText,
  TmplAstTemplate,
  TmplAstElement,
  TmplAstTextAttribute,
} from "@angular/compiler";

// -------------------- CLI --------------------

type CliOptions = {
  tsconfigPath: string;
  cwd: string;
  ignoreSpecs: boolean;
  failOnUnused: boolean;
};

function parseArgs(argv: string[]): CliOptions {
  const cwd = process.cwd();
  const get = (key: string) => {
    const idx = argv.indexOf(key);
    return idx >= 0 ? argv[idx + 1] : undefined;
  };

  const tsconfigPath = get("--tsconfig") ?? "tsconfig.json";
  return {
    tsconfigPath: path.isAbsolute(tsconfigPath) ? tsconfigPath : path.join(cwd, tsconfigPath),
    cwd,
    ignoreSpecs: argv.includes("--ignore-specs") || !argv.includes("--include-specs"),
    failOnUnused: !argv.includes("--no-fail"),
  };
}

// -------------------- Data model --------------------

type StoreInfo = {
  storeName: string;
  storeVarDecl: ts.VariableDeclaration;
  storeType: ts.Type;
  sourceFile: ts.SourceFile;
};

type MethodDef = {
  storeName: string;
  methodName: string;
  node: ts.Node; // property/method node in object literal
  sourceFile: ts.SourceFile;
};

type Usage = {
  storeName: string;
  methodName: string;
  via: "ts" | "template";
  filePath: string;
};

type StoreIndex = {
  stores: Map<string, StoreInfo>;
  methodsByStore: Map<string, MethodDef[]>;
  methodsByName: Map<string, Set<string>>; // methodName -> storeNames that define it
};

// -------------------- TS Program creation --------------------

function loadTsProgram(tsconfigPath: string): { program: ts.Program; checker: ts.TypeChecker } {
  const configFile = ts.readConfigFile(tsconfigPath, ts.sys.readFile);
  if (configFile.error) {
    throw new Error(ts.flattenDiagnosticMessageText(configFile.error.messageText, "\n"));
  }
  const configDir = path.dirname(tsconfigPath);
  const parsed = ts.parseJsonConfigFileContent(
    configFile.config,
    ts.sys,
    configDir,
    /*existingOptions*/ undefined,
    tsconfigPath
  );
  if (parsed.errors.length) {
    const msg = parsed.errors
      .map((d) => ts.flattenDiagnosticMessageText(d.messageText, "\n"))
      .join("\n");
    throw new Error(msg);
  }

  const program = ts.createProgram({
    rootNames: parsed.fileNames,
    options: parsed.options,
  });
  const checker = program.getTypeChecker();
  return { program, checker };
}

// -------------------- Helpers --------------------

function isIdentifierNamed(node: ts.Node | undefined, name: string): node is ts.Identifier {
  return !!node && ts.isIdentifier(node) && node.text === name;
}

function getCallCalleeIdentifierName(call: ts.CallExpression): string | null {
  const callee = call.expression;
  if (ts.isIdentifier(callee)) return callee.text;
  // Support namespace import usage: signals.withMethods(...)
  if (ts.isPropertyAccessExpression(callee) && ts.isIdentifier(callee.name)) return callee.name.text;
  return null;
}

function tryGetObjectLiteralReturnedBy(fn: ts.Expression): ts.ObjectLiteralExpression | null {
  if (ts.isArrowFunction(fn)) {
    if (ts.isObjectLiteralExpression(fn.body)) return fn.body;
    if (ts.isBlock(fn.body)) {
      for (const stmt of fn.body.statements) {
        if (ts.isReturnStatement(stmt) && stmt.expression && ts.isObjectLiteralExpression(stmt.expression)) {
          return stmt.expression;
        }
      }
    }
  }
  if (ts.isFunctionExpression(fn)) {
    for (const stmt of fn.body.statements) {
      if (ts.isReturnStatement(stmt) && stmt.expression && ts.isObjectLiteralExpression(stmt.expression)) {
        return stmt.expression;
      }
    }
  }
  return null;
}

function getObjectLiteralKeyText(name: ts.PropertyName | ts.BindingName | undefined): string | null {
  if (!name) return null;
  if (ts.isIdentifier(name)) return name.text;
  if (ts.isStringLiteral(name)) return name.text;
  if (ts.isNumericLiteral(name)) return name.text;
  return null;
}

function getFilePath(sf: ts.SourceFile): string {
  return sf.fileName;
}

function isDtsOrNodeModules(sf: ts.SourceFile): boolean {
  return sf.isDeclarationFile || sf.fileName.includes("/node_modules/");
}

function isSpecFile(fileName: string): boolean {
  return /\.spec\.ts$/i.test(fileName);
}

function typeMatchesStore(checker: ts.TypeChecker, receiverType: ts.Type, storeType: ts.Type): boolean {
  // Be pragmatic: allow either direction to handle unions / contextual typing.
  return (
    checker.isTypeAssignableTo(receiverType, storeType) ||
    checker.isTypeAssignableTo(storeType, receiverType) ||
    receiverType.flags === ts.TypeFlags.Any ||
    receiverType.flags === ts.TypeFlags.Unknown
  );
}

function safeReadFile(p: string): string | null {
  try {
    return fs.readFileSync(p, "utf8");
  } catch {
    return null;
  }
}

// -------------------- Phase 1: Collect stores + withMethods keys --------------------

function buildStoreIndex(program: ts.Program, checker: ts.TypeChecker, ignoreSpecs: boolean): StoreIndex {
  const stores = new Map<string, StoreInfo>();
  const methodsByStore = new Map<string, MethodDef[]>();
  const methodsByName = new Map<string, Set<string>>();

  for (const sf of program.getSourceFiles()) {
    if (isDtsOrNodeModules(sf)) continue;
    if (ignoreSpecs && isSpecFile(sf.fileName)) continue;

    const visit = (node: ts.Node) => {
      // Detect: export const X = signalStore(...)
      if (ts.isVariableDeclaration(node) && ts.isIdentifier(node.name) && node.initializer) {
        const init = node.initializer;
        if (ts.isCallExpression(init) && getCallCalleeIdentifierName(init) === "signalStore") {
          const storeName = node.name.text;
          const storeType = checker.getTypeAtLocation(node.name);

          stores.set(storeName, {
            storeName,
            storeVarDecl: node,
            storeType,
            sourceFile: sf,
          });

          // Extract withMethods(...) among signalStore arguments
          for (const arg of init.arguments) {
            if (!ts.isCallExpression(arg)) continue;
            if (getCallCalleeIdentifierName(arg) !== "withMethods") continue;

            const factory = arg.arguments[0];
            if (!factory) continue;
            const returnedObj = tryGetObjectLiteralReturnedBy(factory);
            if (!returnedObj) continue;

            const methodDefs: MethodDef[] = methodsByStore.get(storeName) ?? [];
            for (const element of returnedObj.properties) {
              if (ts.isSpreadAssignment(element)) continue;

              // Method shorthand: foo() {}
              if (ts.isMethodDeclaration(element)) {
                const methodName = getObjectLiteralKeyText(element.name);
                if (!methodName) continue;
                methodDefs.push({ storeName, methodName, node: element, sourceFile: sf });
              }

              // Property assignment: foo: (...) => ...
              if (ts.isPropertyAssignment(element)) {
                const methodName = getObjectLiteralKeyText(element.name);
                if (!methodName) continue;
                methodDefs.push({ storeName, methodName, node: element, sourceFile: sf });
              }
            }

            if (methodDefs.length) {
              methodsByStore.set(storeName, methodDefs);
              for (const def of methodDefs) {
                const set = methodsByName.get(def.methodName) ?? new Set<string>();
                set.add(storeName);
                methodsByName.set(def.methodName, set);
              }
            }
          }
        }
      }

      ts.forEachChild(node, visit);
    };

    ts.forEachChild(sf, visit);
  }

  return { stores, methodsByStore, methodsByName };
}

// -------------------- Phase 2: Scan TS usage --------------------

function scanTypeScriptUsages(
  program: ts.Program,
  checker: ts.TypeChecker,
  index: StoreIndex,
  ignoreSpecs: boolean
): Set<string> {
  // key: `${storeName}::${methodName}`
  const used = new Set<string>();

  function mark(storeName: string, methodName: string) {
    used.add(`${storeName}::${methodName}`);
  }

  for (const sf of program.getSourceFiles()) {
    if (isDtsOrNodeModules(sf)) continue;
    if (ignoreSpecs && isSpecFile(sf.fileName)) continue;

    const visit = (node: ts.Node) => {
      // Property access: obj.foo
      if (ts.isPropertyAccessExpression(node)) {
        const prop = node.name.text;
        const storeNames = index.methodsByName.get(prop);
        if (storeNames && storeNames.size) {
          const receiverType = checker.getTypeAtLocation(node.expression);
          for (const storeName of storeNames) {
            const store = index.stores.get(storeName);
            if (!store) continue;
            if (typeMatchesStore(checker, receiverType, store.storeType)) {
              mark(storeName, prop);
            }
          }
        }
      }

      // Element access: obj["foo"]
      if (ts.isElementAccessExpression(node)) {
        const arg = node.argumentExpression;
        if (arg && ts.isStringLiteral(arg)) {
          const prop = arg.text;
          const storeNames = index.methodsByName.get(prop);
          if (storeNames && storeNames.size) {
            const receiverType = checker.getTypeAtLocation(node.expression);
            for (const storeName of storeNames) {
              const store = index.stores.get(storeName);
              if (!store) continue;
              if (typeMatchesStore(checker, receiverType, store.storeType)) {
                mark(storeName, prop);
              }
            }
          }
        }
      }

      // Destructuring: const { foo, bar: baz } = store;
      if (ts.isVariableDeclaration(node) && node.initializer && ts.isObjectBindingPattern(node.name)) {
        const initType = checker.getTypeAtLocation(node.initializer);
        for (const el of node.name.elements) {
          const propName = el.propertyName ? getObjectLiteralKeyText(el.propertyName) : getObjectLiteralKeyText(el.name);
          if (!propName) continue;

          const storeNames = index.methodsByName.get(propName);
          if (!storeNames) continue;

          for (const storeName of storeNames) {
            const store = index.stores.get(storeName);
            if (!store) continue;
            if (typeMatchesStore(checker, initType, store.storeType)) {
              mark(storeName, propName);
            }
          }
        }
      }

      ts.forEachChild(node, visit);
    };

    ts.forEachChild(sf, visit);
  }

  return used;
}

// -------------------- Phase 3: Scan template usage --------------------

type ComponentTemplate = {
  componentFile: string;
  templateFile: string; // same as componentFile for inline templates (for better reporting)
  templateText: string;
  storeProps: Map<string, string>; // componentPropName -> storeName
};

function collectComponentsWithTemplates(
  program: ts.Program,
  index: StoreIndex,
  ignoreSpecs: boolean
): ComponentTemplate[] {
  const results: ComponentTemplate[] = [];

  for (const sf of program.getSourceFiles()) {
    if (isDtsOrNodeModules(sf)) continue;
    if (ignoreSpecs && isSpecFile(sf.fileName)) continue;

    const visit = (node: ts.Node) => {
      if (!ts.isClassDeclaration(node) || !node.name) {
        ts.forEachChild(node, visit);
        return;
      }

      const decorators = ts.canHaveDecorators(node) ? ts.getDecorators(node) : undefined;
      if (!decorators || decorators.length === 0) {
        ts.forEachChild(node, visit);
        return;
      }

      let componentMeta: ts.ObjectLiteralExpression | null = null;

      for (const d of decorators) {
        const expr = d.expression;
        if (!ts.isCallExpression(expr)) continue;
        if (getCallCalleeIdentifierName(expr) !== "Component") continue;
        const arg0 = expr.arguments[0];
        if (arg0 && ts.isObjectLiteralExpression(arg0)) {
          componentMeta = arg0;
          break;
        }
      }

      if (!componentMeta) {
        ts.forEachChild(node, visit);
        return;
      }

      // Resolve template
      let inlineTemplate: string | null = null;
      let templateUrl: string | null = null;

      for (const prop of componentMeta.properties) {
        if (!ts.isPropertyAssignment(prop) || !ts.isIdentifier(prop.name)) continue;
        if (prop.name.text === "template" && ts.isNoSubstitutionTemplateLiteral(prop.initializer)) {
          inlineTemplate = prop.initializer.text;
        }
        if (prop.name.text === "template" && ts.isStringLiteral(prop.initializer)) {
          inlineTemplate = prop.initializer.text;
        }
        if (prop.name.text === "templateUrl" && ts.isStringLiteral(prop.initializer)) {
          templateUrl = prop.initializer.text;
        }
      }

      let templateText: string | null = null;
      let templateFile: string = sf.fileName;

      if (inlineTemplate != null) {
        templateText = inlineTemplate;
        templateFile = sf.fileName;
      } else if (templateUrl) {
        const resolved = path.resolve(path.dirname(sf.fileName), templateUrl);
        const content = safeReadFile(resolved);
        if (content != null) {
          templateText = content;
          templateFile = resolved;
        }
      }

      if (!templateText) {
        ts.forEachChild(node, visit);
        return;
      }

      // Map component properties that inject stores:
      // e.g. readonly booksStore = inject(BooksStore)
      const storeProps = new Map<string, string>();

      for (const member of node.members) {
        if (!ts.isPropertyDeclaration(member)) continue;
        if (!member.name || !ts.isIdentifier(member.name)) continue;
        if (!member.initializer || !ts.isCallExpression(member.initializer)) continue;

        const initCall = member.initializer;
        if (getCallCalleeIdentifierName(initCall) !== "inject") continue;

        const tok = initCall.arguments[0];
        if (!tok || !ts.isIdentifier(tok)) continue;

        const storeName = tok.text;
        if (!index.stores.has(storeName)) continue;

        storeProps.set(member.name.text, storeName);
      }

      if (storeProps.size > 0) {
        results.push({
          componentFile: sf.fileName,
          templateFile,
          templateText,
          storeProps,
        });
      }

      ts.forEachChild(node, visit);
    };

    ts.forEachChild(sf, visit);
  }

  return results;
}

function scanTemplateUsages(components: ComponentTemplate[], index: StoreIndex): Set<string> {
  const used = new Set<string>();

  function mark(storeName: string, methodName: string) {
    used.add(`${storeName}::${methodName}`);
  }

  class UsageVisitor extends RecursiveAstVisitor {
    constructor(private readonly storeProps: Map<string, string>) {
      super();
    }

    override visitCall(ast: NgCall, context: any) {
      const hit = extractStoreMethodFromReceiver(ast.receiver, this.storeProps);
      if (hit) {
        const { storeName, methodName } = hit;
        if (index.methodsByName.get(methodName)?.has(storeName)) {
          mark(storeName, methodName);
        }
      }
      super.visitCall(ast, context);
    }

    override visitPropertyRead(ast: PropertyRead, context: any) {
      const hit = extractStoreMethodFromReceiver(ast, this.storeProps);
      if (hit) {
        const { storeName, methodName } = hit;
        if (index.methodsByName.get(methodName)?.has(storeName)) {
          mark(storeName, methodName);
        }
      }
      super.visitPropertyRead(ast, context);
    }

    override visitSafePropertyRead(ast: SafePropertyRead, context: any) {
      const hit = extractStoreMethodFromReceiver(ast, this.storeProps);
      if (hit) {
        const { storeName, methodName } = hit;
        if (index.methodsByName.get(methodName)?.has(storeName)) {
          mark(storeName, methodName);
        }
      }
      super.visitSafePropertyRead(ast, context);
    }
  }

  for (const c of components) {
    const parsed = parseTemplate(c.templateText, c.templateFile, { preserveWhitespaces: false });
    const visitor = new UsageVisitor(c.storeProps);

    // walk template nodes and visit attached expressions
    walkTemplateNodes(parsed.nodes, (n) => {
      // bound event: (click)="..."
      if (n instanceof TmplAstBoundEvent) {
        const handlerAst = (n.handler as any).ast || n.handler;
        if (handlerAst) visitor.visit(handlerAst as AST, null);
      }
      // bound attribute: [x]="..."
      if (n instanceof TmplAstBoundAttribute) {
        const valueAst = (n.value as any)?.ast || n.value;
        if (valueAst) visitor.visit(valueAst as AST, null);
      }
      // interpolation: {{ ... }}
      if (n instanceof TmplAstBoundText) {
        const textAst = (n.value as any).ast || n.value;
        if (textAst) visitor.visit(textAst as AST, null);
      }
      // ignore plain text attrs
      if (n instanceof TmplAstTextAttribute) {
        // nothing
      }
    });
  }

  return used;
}

function walkTemplateNodes(nodes: TmplAstNode[], fn: (n: any) => void) {
  for (const n of nodes) {
    fn(n);
    if (n instanceof TmplAstElement || n instanceof TmplAstTemplate) {
      walkTemplateNodes(n.children, fn);
      // For elements, also inputs/outputs are already TmplAstBoundAttribute/Event
      // which are in `attributes/inputs/outputs` arrays in older compiler AST,
      // but in current @angular/compiler they appear in `inputs/outputs` fields.
      const anyN: any = n as any;
      const more: any[] = [
        ...(anyN.inputs ?? []),
        ...(anyN.outputs ?? []),
        ...(anyN.attributes ?? []),
        ...(anyN.references ?? []),
        ...(anyN.variables ?? []),
      ];
      for (const x of more) fn(x);
    }
  }
}

function extractStoreMethodFromReceiver(
  receiver: any,
  storeProps: Map<string, string>
): { storeName: string; methodName: string } | null {
  // We want chains like: <storeProp>.<method>
  // In Angular AST:
  // PropertyRead(receiver: PropertyRead(receiver: ImplicitReceiver, name: storeProp), name: method)
  const isPropRead = receiver instanceof PropertyRead || receiver instanceof SafePropertyRead;
  if (!isPropRead) return null;

  const methodName = receiver.name;
  const left = receiver.receiver;

  const leftIsPropRead = left instanceof PropertyRead || left instanceof SafePropertyRead;
  if (!leftIsPropRead) return null;

  // left.name is the root identifier in template scope
  const rootName = left.name;

  // Heuristic: left.receiver should be ImplicitReceiver-ish (we don't import that class here)
  // so we just require rootName to be a mapped store prop.
  const storeName = storeProps.get(rootName);
  if (!storeName) return null;

  return { storeName, methodName };
}

// -------------------- Reporting --------------------

function getLineCol(sf: ts.SourceFile, pos: number): { line: number; col: number } {
  const lc = sf.getLineAndCharacterOfPosition(pos);
  return { line: lc.line + 1, col: lc.character + 1 };
}

function main() {
  const opts = parseArgs(process.argv.slice(2));
  const { program, checker } = loadTsProgram(opts.tsconfigPath);

  const index = buildStoreIndex(program, checker, opts.ignoreSpecs);

  const usedTs = scanTypeScriptUsages(program, checker, index, opts.ignoreSpecs);
  const components = collectComponentsWithTemplates(program, index, opts.ignoreSpecs);
  const usedTpl = scanTemplateUsages(components, index);

  const usedAll = new Set<string>([...usedTs, ...usedTpl]);

  const unused: MethodDef[] = [];
  for (const [storeName, defs] of index.methodsByStore.entries()) {
    for (const def of defs) {
      if (!usedAll.has(`${storeName}::${def.methodName}`)) {
        unused.push(def);
      }
    }
  }

  if (unused.length === 0) {
    console.log("✅ No unused SignalStore withMethods methods found.");
    process.exit(0);
  }

  console.log(`❌ Unused SignalStore methods (${unused.length}):`);
  for (const def of unused) {
    const sf = def.sourceFile;
    const p = getFilePath(sf);
    const pos = def.node.getStart(sf, false);
    const { line, col } = getLineCol(sf, pos);
    console.log(`- ${def.storeName}.${def.methodName}  (${p}:${line}:${col})`);
  }

  process.exit(opts.failOnUnused ? 1 : 0);
}

main();
