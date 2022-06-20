package de.ma.app.utils.sql

import de.ma.app.utils.sql.Sql
import io.quarkus.test.junit.callback.QuarkusTestAfterEachCallback
import io.quarkus.test.junit.callback.QuarkusTestMethodContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

class SqlQuarkusTestAfterEachCallback: QuarkusTestAfterEachCallback{
    override fun afterEach(context: QuarkusTestMethodContext) = runBlocking(
        Dispatchers.IO
    ) {
        //get the package of the context.testInstance
        var packageName = context.testInstance::class.qualifiedName!!.substringBeforeLast('.')

        //replace the '.' in packageName with '/' and add the prefix 'sql/'
        packageName = "/sql/${packageName.replace('.', '/')}"

        val sqlAnnotations = context.testMethod.getAnnotationsByType(Sql::class.java)
        if (sqlAnnotations.isNotEmpty()) {
            for (sqlAnnotation in sqlAnnotations) {
                val targetFiles = sqlAnnotation.after
                for (targetFile in targetFiles) {
                    //put the package name add the target file name
                    SqlFileProcessor.processTargetFile("$packageName/$targetFile")
                }
            }
        }


    }

}