create schema if not exists sample_ns;
create table sample_ns.customer (
    c_custkey int,
    c_name text,
    c_address text,
    c_nationkey int,
    c_phone text,
    c_acctbal double precision,
    c_mktsegment text,
    c_comment text,
    PRIMARY KEY (c_custkey)
);
\copy sample_ns.customer from '/opt/customer.csv' delimiter ',' csv;
