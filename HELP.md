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

`STRIPE_WEBHOOK_SECRET` goes into the run configuration of `GreenshopApplication` as well. It
is the `whsec_...` that `stripe listen` prints when it starts, or `stripe listen --print-secret`
without starting. It usually stays the same between runs, so it is set once. An endpoint created in
the Dashboard has a secret of its own, and that one does not verify what the CLI forwards.

## Building and running it

    ./mvnw verify              # build and all tests, without Stripe
    ./mvnw spring-boot:run     # needs STRIPE_SECRET_KEY and STRIPE_WEBHOOK_SECRET
    stripe listen --forward-to localhost:8484/stripe/webhook
    stripe trigger checkout.session.completed

`stripe listen` runs in a terminal of its own for as long as webhooks are wanted, and shows
every event it forwards with the status the application answered with. `stripe trigger`
creates a whole purchase in the sandbox, with a product of its own, and so sends the same
events as a purchase through the browser.

- **Port**: 8484, overridable with `PORT`.
- **Base URL**: `greenshop.base-url`, by default `http://localhost:<port>`, overridable with
  `BASE_URL`. Stripe sends the customer back to addresses below it.
- **Logging**: to the console. `de.ostfale.greenshop` logs on DEBUG and everything else on
  INFO, so the application's own lines are not drowned out by Spring and Tomcat.
- **Version**: the banner shows the version from the POM. Maven filters it into
  `application.yml` at build time (`@project.version@`), so a change needs a rebuild.
  `build-info` writes it into the jar as well.

## Tests

- **Unit tests** for the records: `Product`, `Money`, `Order` with its status changes, and
  the two properties records.
- **Service tests** against fakes of the outgoing ports, such as `FakeProductCatalog`. No
  mocking framework: a fake is a small class that holds what the test puts in.
- **Web tests** with `@WebMvcTest` and a fake of the incoming port. They parse the rendered
  page with jsoup and check what is on it, not the model.
- **`StripeWebhookControllerTest`**: signs its messages the way Stripe does (HMAC-SHA256 over
  `timestamp.payload`), so the real signature check runs without Stripe. A message signed
  with another secret or changed after signing is refused.
- **`ArchitectureTest`**: the Stripe SDK stays inside its two adapters (see below).
- **`GreenshopApplicationTests`**: the context starts, with a dummy key and secret.
- **`StripeProductCatalogIT`** and **`StripePaymentPageIT`**: talk to the real sandbox. They run only where
  `STRIPE_SECRET_KEY` is set and skips itself otherwise. Maven's surefire plugin does not
  pick up `*IT` classes, so `mvnw verify` never needs Stripe.

What the application sent is visible afterwards in the Dashboard under *Developers → Logs*.
Stripe's SDK does not log requests itself.

## The learning path

| Step | What | State |
|------|------|-------|
| 1 | Stripe account, sandbox, CLI | done |
| 2 | Project skeleton, `StripeClient` as a bean | done |
| 3 | Read products and prices, show them on a page | done |
| 4 | Stripe Checkout: a checkout session, success and cancel pages | done |
| 5 | Webhooks: `checkout.session.completed` marks an order paid | done |
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
    ├── adapter       in.web, in.stripe, out.stripe, out.memory
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

The payment page does not trust the catalog it showed a minute ago, and it does not trust
the browser either. The form sends only the product id. `StripePaymentPage` fetches that
product with its default price again and opens the checkout session with that price. One call
more per purchase, and the amount charged is always Stripe's current one. The rule for what
counts as sellable (a fixed amount, paid once) is in `StripePrices` and serves both.

The quantity is chosen on Stripe's page, not in the shop: the line item carries
`adjustable_quantity` from 1 to 10, and Stripe adds up the total. That keeps the form at a
single field and the browser out of anything that changes the amount. The thank-you page
reads the quantity back from the line items. A cart with several products would be a feature
of its own, since it needs somewhere to live between the pages.

### The way back from Stripe shows, it does not decide

Stripe sends the customer back to `/checkout/success?session_id=...` or to
`/checkout/cancel`. The addresses and the parameter name are in `config.ReturnAddresses`,
which the web adapter serves and the Stripe adapter hands out, so the two cannot drift apart.
The thank-you page looks the session up (with `expand` on `line_items`) and shows what Stripe
reports right now, including "noch nicht bezahlt". It never marks anything as paid: the
customer may close the tab before coming back, and anybody can call the address with any id.
That is step 5, the webhook. The buy form is a POST answered with **303 See Other**, so the
browser follows it with a GET and a reload does not send the form again.

### The webhook decides

An order comes into being only through Stripe's own message to `POST /stripe/webhook`, which
Stripe sends whatever the customer's browser does. `docs/payment-flow.md` draws the whole
exchange, from the catalog to this message.

- **Signature first.** The body is taken as the raw string it arrived as, because the
  signature is computed over exactly those bytes. `Webhook.constructEvent` checks it against
  `stripe.webhook-secret`. Without a valid signature the answer is 400 and nothing is read.
- **Only the id is taken from the event**, out of the raw JSON. The SDK can unpack the event's
  object only when the event was written in the API version the SDK was built for, and the
  sandbox writes events in the account's version. The id is there in every version, and the
  service looks the session up fresh through the port it already has.
- **Three types count.** `checkout.session.completed` places the order: `PAID` when the money
  is in at the till (a card), `AWAITING_PAYMENT` when it comes later (a debit). The two
  `checkout.session.async_payment_*` events settle a waiting order. Every other type is
  acknowledged with 200 and dropped.
- **Twice and out of order is normal.** Stripe delivers at least once and does not promise
  the order. The session id is the order's reference, so a second message finds the order
  and changes nothing. A late verdict that arrives before its checkout places the order
  itself. Step 6 looks at idempotency beyond this.
- **Retry or not.** When Stripe cannot be asked about the session, the answer is 500 and
  Stripe tries again later (a live endpoint for up to three days; `stripe listen` does not
  retry, there `stripe events resend <evt_...>` sends an event again by hand). When the message contradicts the order, such as
  "failed" for a paid one, it is acknowledged, because asking again would not change the
  answer.
- **Lines are copied.** The order keeps the names and quantities as they were sold, not
  references to the catalog.

### Orders in memory, for now

`Orders` is a port. For now `adapter.out.memory.InMemoryOrders` implements it, as a
`ConcurrentHashMap` that is gone with every restart. That keeps step 5 about the webhook and
not about a database. PostgreSQL can take the same port later without the service noticing.

Trying it: `4242 4242 4242 4242` pays, `4000 0027 6000 3184` asks for 3-D Secure, and
`4000 0000 0000 9995` is declined. Any future date and any CVC work.

### Money in the smallest unit

`Money` holds a `long` in the smallest unit of its currency (3,00 € is `300`), exactly as
Stripe counts, with a `java.util.Currency`. It never formats itself: turning `300 EUR` into
"3,00 €" is the page's business. The templates call `@prices.format(...)`, a small bean in
the web adapter. It takes the number of decimals from the currency, not a fixed division
by 100.

### Test keys only, and never in a log

`StripeProperties` refuses at startup anything that does not start with `sk_test_`. That
covers a live key, a missing one, and the unresolved placeholder `${STRIPE_SECRET_KEY}`,
which Spring would otherwise bind as plain text. The key is never logged whole. The startup
line shows its last four characters. A record prints all of its fields in `toString()`, so
`StripeProperties` is never logged as a whole either.

### The house style of greenroom

greenshop looks like greenroom: the same colours, the two fonts Archivo and JetBrains Mono
(both under the SIL Open Font License, served from `static/fonts`), the white bar across the
top with the green wordmark, cards on a grey-green ground. `static/css/greenshop.css` is a
**copy, not a shared file**. It holds only the rules greenshop uses, and a rule from greenroom
comes over when a page needs it. Two projects with one user do not justify a design-system
artifact. Every page takes its `head` and `topbar` from `templates/fragments/layout.html`.

### Versions

Versions of libraries and plugins sit in POM properties, so that the Versions plugin can
update them. Stable releases only, no betas. `stripe-java` is updated deliberately: each
release is pinned to a Stripe API version, and a new one can change what the API returns.
