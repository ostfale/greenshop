package de.ostfale.greenshop;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(
        packages = "de.ostfale.greenshop",
        importOptions = ImportOption.DoNotIncludeTests.class)
public class ArchitectureTest {

    /**
     * Stripe is known only where Stripe is the other side: the webhook coming in and the
     * calls going out. Domain, services and the shop pages see the ports, never the SDK.
     */
    @ArchTest
    static final ArchRule stripeStaysInItsAdapters = noClasses()
            .that().resideOutsideOfPackages(
                    "..greenshop.adapter.in.stripe..",
                    "..greenshop.adapter.out.stripe..")
            .should().dependOnClassesThat().resideInAPackage("com.stripe..");
}
