package com.drakalabs.schoolmngsys.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import jakarta.persistence.Entity;

/**
 * Enforces the module boundary rules from docs/08 §2 that review alone can't guarantee:
 * shared depends on nothing, repositories stay private to their module, entities never leak
 * into {@code api}, and {@code service} never depends on {@code api} (no DTO leaking inward).
 */
@AnalyzeClasses(packages = "com.drakalabs.schoolmngsys", importOptions = ImportOption.DoNotIncludeTests.class)
class ModuleBoundaryArchTest {

    private static final String BASE_PACKAGE = "com.drakalabs.schoolmngsys.";

    @ArchTest
    static final ArchRule shared_depends_on_nothing =
            noClasses()
                    .that()
                    .resideInAPackage("..shared..")
                    .should()
                    .dependOnClassesThat(residesInAnotherSchoolMngSysModule("shared"));

    @ArchTest
    static final ArchRule repositories_are_private_to_their_module =
            classes()
                    .that()
                    .resideInAPackage("..repository..")
                    .should(onlyBeAccessedFromTheSameModule());

    @ArchTest
    static final ArchRule no_entities_in_api =
            noClasses()
                    .that()
                    .resideInAPackage("..api..")
                    .should()
                    .dependOnClassesThat()
                    .areAnnotatedWith(Entity.class)
                    .allowEmptyShould(true); // no `api` package exists yet — starts biting from WP-1 onward

    @ArchTest
    static final ArchRule service_does_not_depend_on_api =
            noClasses()
                    .that()
                    .resideInAPackage("..service..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAPackage("..api..")
                    .allowEmptyShould(true);

    private static DescribedPredicate<JavaClass> residesInAnotherSchoolMngSysModule(String ownModule) {
        return new DescribedPredicate<>("resides in a schoolmngsys module other than " + ownModule) {
            @Override
            public boolean test(JavaClass javaClass) {
                String packageName = javaClass.getPackageName();
                if (!packageName.startsWith(BASE_PACKAGE)) {
                    return false;
                }
                return !topLevelModule(packageName).equals(ownModule);
            }
        };
    }

    private static ArchCondition<JavaClass> onlyBeAccessedFromTheSameModule() {
        return new ArchCondition<>("only be accessed by classes in the same module") {
            @Override
            public void check(JavaClass repositoryClass, ConditionEvents events) {
                String module = topLevelModule(repositoryClass.getPackageName());
                repositoryClass
                        .getAccessesToSelf()
                        .forEach(
                                access -> {
                                    JavaClass origin = access.getOriginOwner();
                                    boolean sameModule = module.equals(topLevelModule(origin.getPackageName()));
                                    String message =
                                            String.format(
                                                    "%s accesses %s (module '%s') from module '%s'",
                                                    origin.getFullName(),
                                                    repositoryClass.getFullName(),
                                                    module,
                                                    topLevelModule(origin.getPackageName()));
                                    events.add(new SimpleConditionEvent(access, sameModule, message));
                                });
            }
        };
    }

    private static String topLevelModule(String packageName) {
        if (!packageName.startsWith(BASE_PACKAGE)) {
            return packageName;
        }
        String rest = packageName.substring(BASE_PACKAGE.length());
        int dot = rest.indexOf('.');
        return dot == -1 ? rest : rest.substring(0, dot);
    }
}
