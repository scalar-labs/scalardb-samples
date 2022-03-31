import yargs from 'yargs/yargs';
import { hideBin } from 'yargs/helpers';
import { GraphQLClient } from 'graphql-request';
import { GraphQLError } from 'graphql-request/dist/types';
import {
  getSdk,
  GetAccountQuery,
  PutAccountMutation,
  GetTwoAccountsQuery,
  PutTwoAccountsMutation,
} from './generated/graphql';

const client = new GraphQLClient('http://localhost:8080/graphql');
const sdk = getSdk(client);

yargs(hideBin(process.argv))
  .command('show <user>', 'Show the account of <user>', {}, (argv) => {
    show(argv.user as string);
  })
  .command(
    'charge <user> <amount>',
    'Charge the <amount> to the account for <user>',
    {},
    (argv) => {
      console.info(`Charging the amount ${argv.amount} to ${argv.user}`);
      charge(argv.user as string, argv.amount as number);
    }
  )
  .command(
    'pay <from_user> <to_user> <amount>',
    'Pay the <amount> from <from_user> to <to_user>',
    {},
    (argv) => {
      console.info(
        `Paying the amount ${argv.amount} from ${argv.from_user} to ${argv.to_user}`
      );
      pay(
        argv.from_user as string,
        argv.to_user as string,
        argv.amount as number
      );
    }
  )
  .demandCommand(1)
  .help()
  .parse();

async function show(id: string): Promise<void> {
  // Retrieve the current balance for id
  const { data, errors } = await sdk.GetAccount({ id, commit: true });
  if (errors) {
    console.error(errors);
    throw new Error('An error happened');
  }
  const account = data?.get1?.account;
  if (account) {
    console.info(`Balance for account ${id}: ${account.balance}`);
  } else {
    console.info(`Account ${id} is not found`);
  }
}

async function charge(id: string, amount: number): Promise<void> {
  let dataGet: GetAccountQuery | undefined,
    dataPut: PutAccountMutation | undefined,
    errors: GraphQLError[] | undefined,
    extensions: { transaction: { id: string } };

  // Retrieve the current balance for id
  ({ data: dataGet, errors, extensions } = await sdk.GetAccount({ id }));

  if (errors) {
    console.error(errors);
    throw new Error('An error happened');
  }

  // Calculate the balance
  const balance = (dataGet!.get1!.account?.balance || 0) + amount;

  // Update the balance and commit the transaction
  ({
    data: dataPut,
    errors,
    extensions,
  } = await sdk.PutAccount({
    id,
    balance,
    txId: extensions!.transaction!.id,
    commit: true,
  }));

  if (errors) {
    console.error(errors);
    throw new Error('An error happened');
  }
}

async function pay(
  from_id: string,
  to_id: string,
  amount: number
): Promise<void> {
  let dataGet: GetTwoAccountsQuery | undefined,
    dataPut: PutTwoAccountsMutation | undefined,
    errors: GraphQLError[] | undefined,
    extensions: { transaction: { id: string } };

  // Retrieve the current balances for given ids
  ({
    data: dataGet,
    errors,
    extensions,
  } = await sdk.GetTwoAccounts({
    id1: from_id,
    id2: to_id,
  }));

  if (errors) {
    console.error(errors);
    throw new Error('An error happened');
  }

  // Calculate the balances
  const balance1 = (dataGet!.get1!.account?.balance || 0) - amount;
  const balance2 = (dataGet!.get2!.account?.balance || 0) + amount;

  // Update the balances and commit the transaction
  const txId = extensions!.transaction!.id;
  ({
    data: dataPut,
    errors,
    extensions,
  } = await sdk.PutTwoAccounts({
    id1: from_id,
    id2: to_id,
    balance1,
    balance2,
    txId,
    commit: true,
  }));

  if (errors) {
    console.error(errors);
    throw new Error('An error happened');
  }
}
