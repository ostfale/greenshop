# greenshop

A small shop for the merchandise of the Java User Group Hamburg, built to learn how the
Stripe API works. The name follows `greenroom`, the group's planning tool. It runs against a
Stripe **sandbox** only: no real money moves, and a live key stops the start.

## What it does

So far:

- **Reads the catalog from Stripe**: every active product with its default price, in one
  call. A product without a usable price is left out.
- **Shows the catalog on `/`**: name and price in German format. When Stripe cannot be
  reached, the page stays and says so.

Next, one step at a time (see `HELP.md` for the full path):

- Buy through Stripe Checkout, and confirm the payment through a webhook, not the
  redirect.
- Refunds and failed payments. Later a supporting membership as a subscription.

## Stack

- **Language and framework**: Java 25, Spring Boot 4, Maven.
- **Payments**: `stripe-java` with `StripeClient`, against a Stripe sandbox.
- **Pages**: Thymeleaf.
- **Tests**: JUnit, AssertJ, ArchUnit, plus one test against the sandbox.
- **Build guards**: Maven Enforcer (Maven, Java 25, dependency convergence) and the
  Versions plugin for updates.

## Tools

- **IntelliJ IDEA**: the Stripe key lives in the run configurations, never in a file.
- **Stripe CLI**: logs in to the sandbox, creates test data, and later forwards webhooks to
  the local application.
- **Stripe Dashboard**: the other side. It shows products, payments, events and every API
  request the application made.

## Running it

    ./mvnw verify              # build and all tests, without Stripe
    ./mvnw spring-boot:run     # needs STRIPE_SECRET_KEY, listens on 8484

## Where the rest is

- **`HELP.md`**: how it is set up and run, the learning path, and the decisions behind the
  design.
