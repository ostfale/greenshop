# greenshop: how it is built and why

How the application is set up, run and tested, the path it is learned along, and the
decisions behind the design. What it is for and what it does is in `README.md`.

## Setting it up

### Stripe

1. A Stripe account with a **sandbox**, here `greenshop Sandbox`. Nothing is activated for
   live payments. Business details, bank account and identity are only asked for when going
   live, and that never happens here.
2. The secret key of that sandbox (`sk_test_...`) under *Developers → API keys*. It is kept
   in a password manager and in the IntelliJ run configurations, nowhere else.
3. The Stripe CLI, logged in to the **same** sandbox with `stripe login`. Every sandbox is a
   world of its own: products created in one are invisible with the key of another.
   `stripe products list` shows whether CLI and key point at the same place.

The CLI gets its own restricted key at login, valid for 90 days. It never needs the secret
key of the application.

### IntelliJ

`STRIPE_SECRET_KEY` goes into the environment variables of the run configuration of
`GreenshopApplication`. For the tests it goes into the **JUnit configuration template**
(*Edit Configurations → Edit configuration templates → JUnit*). A test started by hand gets a
new run configuration of its own, and it inherits the variable only from the template. Both
end up in `.idea/workspace.xml`, which git ignores.

## Building and running it

    ./mvnw verify              # build and all tests, without Stripe
    ./mvnw spring-boot:run     # needs STRIPE_SECRET_KEY

- **Port**: 8484, overridable with `PORT`.
- **Logging**: to the console. `de.ostfale.greenshop` logs on DEBUG and everything else on
  INFO, so the application's own lines are not drowned out by Spring and Tomcat.
- **Version**: the banner shows the version from the POM. Maven filters it into
  `application.yml` at build time (`@project.version@`), so a change needs a rebuild.
  `build-info` writes it into the jar as well.

## Tests

- **Unit tests** for the records: `Product`, `Money`, `StripeProperties`.
- **`ArchitectureTest`**: the Stripe SDK stays inside its two adapters (see below).
- **`GreenshopApplicationTests`**: the context starts, with a dummy key.
- **`StripeProductCatalogIT`**: talks to the real sandbox. It runs only where
  `STRIPE_SECRET_KEY` is set and skips itself otherwise. Maven's surefire plugin does not
  pick up `*IT` classes, so `mvnw verify` never needs Stripe.

What the application sent is visible afterwards in the Dashboard under *Developers → Logs*.
Stripe's SDK does not log requests itself.

## The learning path

| Step | What | State |
|------|------|-------|
| 1 | Stripe account, sandbox, CLI | done |
| 2 | Project skeleton, `StripeClient` as a bean | done |
| 3 | Read products and prices, show them on a page | catalog read, page open |
| 4 | Stripe Checkout: a checkout session, success and cancel pages | |
| 5 | Webhooks: `checkout.session.completed` marks an order paid | |
| 6 | Idempotency, `metadata` and `client_reference_id` | |
| 7 | Declined cards, 3-D Secure, refunds | |
| 8 | A supporting membership as a subscription, Customer Portal | optional |
| 9 | Own payment form with the Payment Element | optional |
| 10 | Webhook tests with signed payloads, Stripe mocked | optional |

## Decisions

### Ports and adapters

    de.ostfale.greenshop
    ├── domain        records and value objects, plain Java
    ├── application   port.in, port.out, service
    ├── adapter       in.web, in.stripe, out.stripe
    └── config

The layout follows `greenroom`. **Stripe is known only where Stripe is the other side**:
`adapter.in.stripe` for the webhook coming in, `adapter.out.stripe` for the calls going out.
Domain, services and the shop pages see ports, never the SDK. The ArchUnit rule
`stripeStaysInItsAdapters` enforces this. A failure crosses the port by name
(`CatalogUnavailable`), with the `StripeException` only as its cause.

### Stripe is the catalog

Products and prices are kept in Stripe, not copied into a database of our own. A second copy
would be a second truth to keep in step. The domain `Product` therefore carries Stripe's id
(`prod_...`). The adapter asks for active products with `expand` on `data.default_price`, so
one request brings the prices along. Without `expand` the price is only an id. A product
without a default price, or with a price that has no fixed amount, is **left out, not guessed
at**.

### Money in the smallest unit

`Money` holds a `long` in the smallest unit of its currency (3,00 € is `300`), exactly as
Stripe counts, with a `java.util.Currency`. It never formats itself: turning `300 EUR` into
"3,00 €" is the page's business.

### Test keys only, and never in a log

`StripeProperties` refuses at startup anything that does not start with `sk_test_`. That
covers a live key, a missing one, and the unresolved placeholder `${STRIPE_SECRET_KEY}`,
which Spring would otherwise bind as plain text. The key is never logged whole. The startup
line shows its last four characters. A record prints all of its fields in `toString()`, so
`StripeProperties` is never logged as a whole either.

### Versions

Versions of libraries and plugins sit in POM properties, so that the Versions plugin can
update them. Stable releases only, no betas. `stripe-java` is updated deliberately: each
release is pinned to a Stripe API version, and a new one can change what the API returns.
