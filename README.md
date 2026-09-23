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
- **Sells through Stripe Checkout**: a buy button per product leads to Stripe's hosted payment
  page, where the customer picks how many (1 to 10) and gives a shipping address in Germany.
  Afterwards a thank-you page shows what was bought and whether Stripe reports it paid. A
  cancel leads back to the catalog.
- **Keeps the orders Stripe confirms**: a signed webhook places the order, paid or waiting
  for its money, and settles a late payment. `/orders` lists them.

- **Does nothing twice**: two clicks on a buy button open one checkout (an idempotency key), a
  webhook delivered again is dropped by its message id, and only sessions marked as this
  shop's own become orders.
- **Keeps what it sold**: orders and the messages already handled live in an H2 file under
  `./data`, built by Flyway and written with plain SQL.

- **Gives money back**: a paid order can be refunded in full from `/orders`, and a refund made
  in Stripe's Dashboard finds its order through the payment it went through.

Next, one step at a time (see `HELP.md` for the full path):

- A supporting membership as a subscription, with the Customer Portal.

## Stack

- **Language and framework**: Java 25, Spring Boot 4, Maven.
- **Payments**: `stripe-java` with `StripeClient`, against a Stripe sandbox.
- **Storage**: H2 as a file, Flyway for the schema, `JdbcClient` for the statements.
- **Pages**: Thymeleaf.
- **Tests**: JUnit, AssertJ, ArchUnit, plus one test against the sandbox.
- **Build guards**: Maven Enforcer (Maven, Java 25, dependency convergence) and the
  Versions plugin for updates.

## Tools

- **IntelliJ IDEA**: the Stripe key lives in the run configurations, never in a file.
- **Stripe CLI**: logs in to the sandbox, creates test data, and forwards webhooks to the
  local application with `stripe listen`.
- **Stripe Dashboard**: the other side. It shows products, payments, events and every API
  request the application made.

## Running it

    ./mvnw verify              # build and all tests, without Stripe
    ./mvnw spring-boot:run     # needs STRIPE_SECRET_KEY and STRIPE_WEBHOOK_SECRET, port 8484
    stripe listen --forward-to localhost:8484/stripe/webhook

## Where the rest is

- **`HELP.md`**: how it is set up and run, the learning path, and the decisions behind the
  design.
- **`docs/payment-flow.md`**: every message between customer, greenshop and Stripe during a
  purchase, as a sequence diagram and step by step.
