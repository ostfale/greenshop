-- What Stripe has confirmed. The reference is Stripe's id of the checkout, the payment its id
-- of what was charged; a refund is reported on the payment, so it is looked up by both.
create table customer_order
(
    reference      varchar(255) primary key,
    payment        varchar(255),
    total_amount   bigint       not null,
    total_currency varchar(3)   not null,
    status         varchar(20)  not null,
    placed_at      timestamp    not null
);

create index idx_customer_order_payment on customer_order (payment);

-- The lines as they were sold: the name then, not a reference to the catalog.
create table order_line
(
    order_reference varchar(255) not null references customer_order (reference) on delete cascade,
    line_no         int          not null,
    name            varchar(255) not null,
    quantity        bigint       not null,
    primary key (order_reference, line_no)
);

-- Which messages of the provider have been dealt with, so that one delivered again is dropped.
create table handled_message
(
    id         varchar(255) primary key,
    handled_at timestamp not null
);
