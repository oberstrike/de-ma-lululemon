package com.mediaserver.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

/**
 * Architecture tests to validate Clean Architecture / Hexagonal Architecture structure.
 *
 * <p>Layer structure:
 *
 * <ul>
 *   <li>Domain: Core business logic, entities, repository interfaces (ports)
 *   <li>Application: Use cases, application services, port interfaces
 *   <li>Infrastructure: Controllers, JPA repositories, external services (adapters)
 * </ul>
 */
class ArchitectureTest {

    private static JavaClasses importedClasses;

    private static final String BASE_PACKAGE = "com.mediaserver";
    private static final String DOMAIN_PACKAGE = BASE_PACKAGE + ".domain..";
    private static final String APPLICATION_PACKAGE = BASE_PACKAGE + ".application..";
    private static final String INFRASTRUCTURE_PACKAGE = BASE_PACKAGE + ".infrastructure..";
    private static final String CONFIG_PACKAGE = BASE_PACKAGE + ".config..";
    private static final String SERVICE_PACKAGE = BASE_PACKAGE + ".service..";
    private static final String EVENT_PACKAGE = BASE_PACKAGE + ".event..";
    private static final String DTO_PACKAGE = BASE_PACKAGE + ".dto..";

    @BeforeAll
    static void setup() {
        importedClasses =
                new ClassFileImporter()
                        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                        .importPackages(BASE_PACKAGE);
    }

    @Nested
    @DisplayName("Layered Architecture Rules")
    class LayeredArchitectureRules {

        @Test
        @DisplayName("Should follow layered architecture")
        void shouldFollowLayeredArchitecture() {
            layeredArchitecture()
                    .consideringAllDependencies()
                    .layer("Domain")
                    .definedBy(DOMAIN_PACKAGE)
                    .layer("Application")
                    .definedBy(APPLICATION_PACKAGE)
                    .layer("Infrastructure")
                    .definedBy(INFRASTRUCTURE_PACKAGE)
                    .layer("Config")
                    .definedBy(CONFIG_PACKAGE)
                    .layer("Service")
                    .definedBy(SERVICE_PACKAGE)
                    .layer("Event")
                    .definedBy(EVENT_PACKAGE)
                    .layer("Dto")
                    .definedBy(DTO_PACKAGE)
                    .whereLayer("Domain")
                    .mayOnlyBeAccessedByLayers(
                            "Application", "Infrastructure", "Config", "Service", "Event", "Dto")
                    .whereLayer("Application")
                    .mayOnlyBeAccessedByLayers("Infrastructure", "Config", "Service")
                    // Infrastructure DTOs can be accessed by Service/Event for now
                    // TODO: Consider moving shared DTOs to application layer
                    .whereLayer("Infrastructure")
                    .mayOnlyBeAccessedByLayers("Service", "Event", "Config")
                    .check(importedClasses);
        }
    }

    @Nested
    @DisplayName("Domain Layer Rules")
    class DomainLayerRules {

        @Test
        @DisplayName("Domain should not depend on application layer")
        void domainShouldNotDependOnApplication() {
            noClasses()
                    .that()
                    .resideInAPackage(DOMAIN_PACKAGE)
                    .should()
                    .dependOnClassesThat()
                    .resideInAPackage(APPLICATION_PACKAGE)
                    .because("Domain layer should be independent of application layer")
                    .check(importedClasses);
        }

        @Test
        @DisplayName("Domain should not depend on infrastructure layer")
        void domainShouldNotDependOnInfrastructure() {
            noClasses()
                    .that()
                    .resideInAPackage(DOMAIN_PACKAGE)
                    .should()
                    .dependOnClassesThat()
                    .resideInAPackage(INFRASTRUCTURE_PACKAGE)
                    .because("Domain layer should be independent of infrastructure layer")
                    .check(importedClasses);
        }

        @Test
        @DisplayName("Domain should not depend on Spring framework")
        void domainShouldNotDependOnSpring() {
            noClasses()
                    .that()
                    .resideInAPackage(DOMAIN_PACKAGE)
                    .should()
                    .dependOnClassesThat()
                    .resideInAPackage("org.springframework..")
                    .because("Domain layer should be framework-agnostic")
                    .check(importedClasses);
        }

        @Test
        @DisplayName("Domain models should reside in domain.model package")
        void domainModelsShouldResideInCorrectPackage() {
            classes()
                    .that()
                    .resideInAPackage("..domain.model..")
                    .should()
                    .haveSimpleNameNotEndingWith("Repository")
                    .because("Repository interfaces should be in domain.repository package")
                    .check(importedClasses);
        }

        @Test
        @DisplayName("Repository interfaces should be in domain.repository package")
        void repositoryInterfacesShouldBeInCorrectPackage() {
            classes()
                    .that()
                    .haveSimpleNameEndingWith("Repository")
                    .and()
                    .resideInAPackage(DOMAIN_PACKAGE)
                    .should()
                    .resideInAPackage("..domain.repository..")
                    .because("Repository interfaces (ports) should be in domain.repository")
                    .check(importedClasses);
        }
    }

    @Nested
    @DisplayName("Application Layer Rules")
    class ApplicationLayerRules {

        @Test
        @DisplayName("Application should not depend on infrastructure layer")
        void applicationShouldNotDependOnInfrastructure() {
            noClasses()
                    .that()
                    .resideInAPackage(APPLICATION_PACKAGE)
                    .should()
                    .dependOnClassesThat()
                    .resideInAPackage(INFRASTRUCTURE_PACKAGE)
                    .because("Application layer should not depend on infrastructure layer")
                    .check(importedClasses);
        }

        @Test
        @DisplayName("Use cases should be interfaces in application.port.in package")
        void useCasesShouldBeInterfacesInPortPackage() {
            classes()
                    .that()
                    .resideInAPackage("..application.port.in..")
                    .and()
                    .areTopLevelClasses()
                    .should()
                    .beInterfaces()
                    .because("Input ports (use cases) should be interfaces")
                    .check(importedClasses);
        }

        @Test
        @DisplayName("Output ports should be interfaces")
        void outputPortsShouldBeInterfaces() {
            classes()
                    .that()
                    .resideInAPackage("..application.port.out..")
                    .should()
                    .beInterfaces()
                    .because("Output ports should be interfaces")
                    .check(importedClasses);
        }

        @Test
        @DisplayName("Application services should implement use cases")
        void applicationServicesShouldResideInCorrectPackage() {
            classes()
                    .that()
                    .resideInAPackage("..application.service..")
                    .and()
                    .haveSimpleNameEndingWith("Service")
                    .should()
                    .beAnnotatedWith(Service.class)
                    .because("Application services should be Spring services")
                    .check(importedClasses);
        }
    }

    @Nested
    @DisplayName("Infrastructure Layer Rules")
    class InfrastructureLayerRules {

        @Test
        @DisplayName("Controllers should be in infrastructure.rest.controller package")
        void controllersShouldBeInCorrectPackage() {
            classes()
                    .that()
                    .haveSimpleNameEndingWith("Controller")
                    .should()
                    .resideInAPackage("..infrastructure.rest.controller..")
                    .because("Controllers should be in infrastructure.rest.controller package")
                    .check(importedClasses);
        }

        @Test
        @DisplayName("Controllers should be annotated with @RestController")
        void controllersShouldBeAnnotatedCorrectly() {
            classes()
                    .that()
                    .resideInAPackage("..infrastructure.rest.controller..")
                    .and()
                    .haveSimpleNameEndingWith("Controller")
                    .should()
                    .beAnnotatedWith(RestController.class)
                    .because("Controllers should be REST controllers")
                    .check(importedClasses);
        }

        @Test
        @DisplayName("JPA entities should be in infrastructure.persistence.entity package")
        void jpaEntitiesShouldBeInCorrectPackage() {
            classes()
                    .that()
                    .haveSimpleNameEndingWith("JpaEntity")
                    .should()
                    .resideInAPackage("..infrastructure.persistence.entity..")
                    .because("JPA entities should be in infrastructure.persistence.entity package")
                    .check(importedClasses);
        }

        @Test
        @DisplayName("JPA repositories should be in infrastructure.persistence.repository package")
        void jpaRepositoriesShouldBeInCorrectPackage() {
            classes()
                    .that()
                    .haveSimpleNameStartingWith("Jpa")
                    .and()
                    .haveSimpleNameEndingWith("Repository")
                    .should()
                    .resideInAPackage("..infrastructure.persistence.repository..")
                    .because("JPA repositories should be in infrastructure.persistence.repository")
                    .check(importedClasses);
        }

        @Test
        @DisplayName("Repository adapters should be in infrastructure.persistence.adapter package")
        void repositoryAdaptersShouldBeInCorrectPackage() {
            classes()
                    .that()
                    .haveSimpleNameEndingWith("RepositoryAdapter")
                    .should()
                    .resideInAPackage("..infrastructure.persistence.adapter..")
                    .because("Repository adapters should be in infrastructure.persistence.adapter")
                    .check(importedClasses);
        }

        @Test
        @DisplayName("Mappers should be in appropriate mapper packages")
        void mappersShouldBeInCorrectPackages() {
            classes()
                    .that()
                    .haveSimpleNameEndingWith("Mapper")
                    .should()
                    .resideInAnyPackage(
                            "..infrastructure.persistence.mapper..",
                            "..infrastructure.rest.mapper..")
                    .because("Mappers should be in mapper packages")
                    .check(importedClasses);
        }
    }

    @Nested
    @DisplayName("Naming Convention Rules")
    class NamingConventionRules {

        @Test
        @DisplayName("DTOs should end with DTO suffix")
        void dtosShouldHaveCorrectSuffix() {
            classes()
                    .that()
                    .resideInAPackage("..dto..")
                    .and()
                    .areTopLevelClasses()
                    .should()
                    .haveSimpleNameEndingWith("DTO")
                    .because("DTOs should have DTO suffix (uppercase)")
                    .check(importedClasses);
        }

        @Test
        @DisplayName("Exceptions should end with Exception suffix")
        void exceptionsShouldHaveCorrectSuffix() {
            classes()
                    .that()
                    .resideInAPackage("..exception..")
                    .and()
                    .areTopLevelClasses()
                    .and()
                    .areNotInterfaces()
                    .should()
                    .haveSimpleNameEndingWith("Exception")
                    .orShould()
                    .haveSimpleNameEndingWith("Handler")
                    .because("Exception classes should have Exception or Handler suffix")
                    .check(importedClasses);
        }
    }

    @Nested
    @DisplayName("Dependency Rules")
    class DependencyRules {

        @Test
        @DisplayName("Controllers should not directly access repositories")
        void controllersShouldNotAccessRepositoriesDirectly() {
            noClasses()
                    .that()
                    .resideInAPackage("..infrastructure.rest.controller..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAPackage("..domain.repository..")
                    .because(
                            "Controllers should use application services, not repositories"
                                    + " directly")
                    .check(importedClasses);
        }

        @Test
        @DisplayName("Controllers should not directly access JPA repositories")
        void controllersShouldNotAccessJpaRepositories() {
            noClasses()
                    .that()
                    .resideInAPackage("..infrastructure.rest.controller..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAPackage("..infrastructure.persistence.repository..")
                    .because("Controllers should not directly access JPA repositories")
                    .check(importedClasses);
        }
    }
}
