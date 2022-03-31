import { GraphQLClient } from 'graphql-request';
import * as Dom from 'graphql-request/dist/types.dom';
import { GraphQLError } from 'graphql-request/dist/types';
import { print } from 'graphql'
import gql from 'graphql-tag';
export type Maybe<T> = T | null;
export type InputMaybe<T> = Maybe<T>;
export type Exact<T extends { [key: string]: unknown }> = { [K in keyof T]: T[K] };
export type MakeOptional<T, K extends keyof T> = Omit<T, K> & { [SubKey in K]?: Maybe<T[SubKey]> };
export type MakeMaybe<T, K extends keyof T> = Omit<T, K> & { [SubKey in K]: Maybe<T[SubKey]> };
/** All built-in and custom scalars, mapped to their actual values */
export type Scalars = {
  ID: string;
  String: string;
  Boolean: boolean;
  Int: number;
  Float: number;
  BigInt: any;
  Float32: any;
};

export type ConditionalExpression = {
  bigIntValue?: InputMaybe<Scalars['BigInt']>;
  booleanValue?: InputMaybe<Scalars['Boolean']>;
  doubleValue?: InputMaybe<Scalars['Float']>;
  floatValue?: InputMaybe<Scalars['Float32']>;
  intValue?: InputMaybe<Scalars['Int']>;
  name: Scalars['String'];
  operator: ConditionalExpressionOperator;
  textValue?: InputMaybe<Scalars['String']>;
};

export enum ConditionalExpressionOperator {
  Eq = 'EQ',
  Gt = 'GT',
  Gte = 'GTE',
  Lt = 'LT',
  Lte = 'LTE',
  Ne = 'NE'
}

export enum Consistency {
  Eventual = 'EVENTUAL',
  Linearizable = 'LINEARIZABLE',
  Sequential = 'SEQUENTIAL'
}

export type DeleteCondition = {
  expressions?: InputMaybe<Array<ConditionalExpression>>;
  type: DeleteConditionType;
};

export enum DeleteConditionType {
  DeleteIf = 'DeleteIf',
  DeleteIfExists = 'DeleteIfExists'
}

export type Mutation = {
  __typename?: 'Mutation';
  abort: Scalars['Boolean'];
  account_bulkDelete: Scalars['Boolean'];
  account_bulkPut: Scalars['Boolean'];
  account_delete: Scalars['Boolean'];
  account_mutate: Scalars['Boolean'];
  account_put: Scalars['Boolean'];
};


export type MutationAccount_BulkDeleteArgs = {
  delete: Array<Account_DeleteInput>;
};


export type MutationAccount_BulkPutArgs = {
  put: Array<Account_PutInput>;
};


export type MutationAccount_DeleteArgs = {
  delete: Account_DeleteInput;
};


export type MutationAccount_MutateArgs = {
  delete?: InputMaybe<Array<Account_DeleteInput>>;
  put?: InputMaybe<Array<Account_PutInput>>;
};


export type MutationAccount_PutArgs = {
  put: Account_PutInput;
};

export enum Order {
  Asc = 'ASC',
  Desc = 'DESC'
}

export type PutCondition = {
  expressions?: InputMaybe<Array<ConditionalExpression>>;
  type: PutConditionType;
};

export enum PutConditionType {
  PutIf = 'PutIf',
  PutIfExists = 'PutIfExists',
  PutIfNotExists = 'PutIfNotExists'
}

export type Query = {
  __typename?: 'Query';
  account_get?: Maybe<Account_GetPayload>;
};


export type QueryAccount_GetArgs = {
  get: Account_GetInput;
};

export type Account = {
  __typename?: 'account';
  balance?: Maybe<Scalars['Int']>;
  id?: Maybe<Scalars['String']>;
};

export type Account_DeleteInput = {
  condition?: InputMaybe<DeleteCondition>;
  consistency?: InputMaybe<Consistency>;
  key: Account_Key;
};

export type Account_GetInput = {
  consistency?: InputMaybe<Consistency>;
  key: Account_Key;
};

export type Account_GetPayload = {
  __typename?: 'account_GetPayload';
  account?: Maybe<Account>;
};

export type Account_Key = {
  id: Scalars['String'];
};

export type Account_PutInput = {
  condition?: InputMaybe<PutCondition>;
  consistency?: InputMaybe<Consistency>;
  key: Account_Key;
  values: Account_PutValues;
};

export type Account_PutValues = {
  balance?: InputMaybe<Scalars['Int']>;
};

export type AccountFieldsFragment = { __typename?: 'account_GetPayload', account?: { __typename?: 'account', id?: string | null, balance?: number | null } | null };

export type GetAccountQueryVariables = Exact<{
  id: Scalars['String'];
  txId?: InputMaybe<Scalars['String']>;
  commit?: InputMaybe<Scalars['Boolean']>;
}>;


export type GetAccountQuery = { __typename?: 'Query', get1?: { __typename?: 'account_GetPayload', account?: { __typename?: 'account', id?: string | null, balance?: number | null } | null } | null };

export type PutAccountMutationVariables = Exact<{
  id: Scalars['String'];
  balance?: InputMaybe<Scalars['Int']>;
  txId?: InputMaybe<Scalars['String']>;
  commit?: InputMaybe<Scalars['Boolean']>;
}>;


export type PutAccountMutation = { __typename?: 'Mutation', put1: boolean };

export type GetTwoAccountsQueryVariables = Exact<{
  id1: Scalars['String'];
  id2: Scalars['String'];
  txId?: InputMaybe<Scalars['String']>;
  commit?: InputMaybe<Scalars['Boolean']>;
}>;


export type GetTwoAccountsQuery = { __typename?: 'Query', get1?: { __typename?: 'account_GetPayload', account?: { __typename?: 'account', id?: string | null, balance?: number | null } | null } | null, get2?: { __typename?: 'account_GetPayload', account?: { __typename?: 'account', id?: string | null, balance?: number | null } | null } | null };

export type PutTwoAccountsMutationVariables = Exact<{
  id1: Scalars['String'];
  balance1?: InputMaybe<Scalars['Int']>;
  id2: Scalars['String'];
  balance2?: InputMaybe<Scalars['Int']>;
  txId?: InputMaybe<Scalars['String']>;
  commit?: InputMaybe<Scalars['Boolean']>;
}>;


export type PutTwoAccountsMutation = { __typename?: 'Mutation', put1: boolean, put2: boolean };

export const AccountFieldsFragmentDoc = gql`
    fragment accountFields on account_GetPayload {
  account {
    id
    balance
  }
}
    `;
export const GetAccountDocument = gql`
    query GetAccount($id: String!, $txId: String, $commit: Boolean) @transaction(id: $txId, commit: $commit) {
  get1: account_get(get: {key: {id: $id}}) {
    ...accountFields
  }
}
    ${AccountFieldsFragmentDoc}`;
export const PutAccountDocument = gql`
    mutation PutAccount($id: String!, $balance: Int, $txId: String, $commit: Boolean) @transaction(id: $txId, commit: $commit) {
  put1: account_put(put: {key: {id: $id}, values: {balance: $balance}})
}
    `;
export const GetTwoAccountsDocument = gql`
    query GetTwoAccounts($id1: String!, $id2: String!, $txId: String, $commit: Boolean) @transaction(id: $txId, commit: $commit) {
  get1: account_get(get: {key: {id: $id1}}) {
    ...accountFields
  }
  get2: account_get(get: {key: {id: $id2}}) {
    ...accountFields
  }
}
    ${AccountFieldsFragmentDoc}`;
export const PutTwoAccountsDocument = gql`
    mutation PutTwoAccounts($id1: String!, $balance1: Int, $id2: String!, $balance2: Int, $txId: String, $commit: Boolean) @transaction(id: $txId, commit: $commit) {
  put1: account_put(put: {key: {id: $id1}, values: {balance: $balance1}})
  put2: account_put(put: {key: {id: $id2}, values: {balance: $balance2}})
}
    `;

export type SdkFunctionWrapper = <T>(action: (requestHeaders?:Record<string, string>) => Promise<T>, operationName: string, operationType?: string) => Promise<T>;


const defaultWrapper: SdkFunctionWrapper = (action, _operationName, _operationType) => action();
const GetAccountDocumentString = print(GetAccountDocument);
const PutAccountDocumentString = print(PutAccountDocument);
const GetTwoAccountsDocumentString = print(GetTwoAccountsDocument);
const PutTwoAccountsDocumentString = print(PutTwoAccountsDocument);
export function getSdk(client: GraphQLClient, withWrapper: SdkFunctionWrapper = defaultWrapper) {
  return {
    GetAccount(variables: GetAccountQueryVariables, requestHeaders?: Dom.RequestInit["headers"]): Promise<{ data?: GetAccountQuery | undefined; extensions?: any; headers: Dom.Headers; status: number; errors?: GraphQLError[] | undefined; }> {
        return withWrapper((wrappedRequestHeaders) => client.rawRequest<GetAccountQuery>(GetAccountDocumentString, variables, {...requestHeaders, ...wrappedRequestHeaders}), 'GetAccount', 'query');
    },
    PutAccount(variables: PutAccountMutationVariables, requestHeaders?: Dom.RequestInit["headers"]): Promise<{ data?: PutAccountMutation | undefined; extensions?: any; headers: Dom.Headers; status: number; errors?: GraphQLError[] | undefined; }> {
        return withWrapper((wrappedRequestHeaders) => client.rawRequest<PutAccountMutation>(PutAccountDocumentString, variables, {...requestHeaders, ...wrappedRequestHeaders}), 'PutAccount', 'mutation');
    },
    GetTwoAccounts(variables: GetTwoAccountsQueryVariables, requestHeaders?: Dom.RequestInit["headers"]): Promise<{ data?: GetTwoAccountsQuery | undefined; extensions?: any; headers: Dom.Headers; status: number; errors?: GraphQLError[] | undefined; }> {
        return withWrapper((wrappedRequestHeaders) => client.rawRequest<GetTwoAccountsQuery>(GetTwoAccountsDocumentString, variables, {...requestHeaders, ...wrappedRequestHeaders}), 'GetTwoAccounts', 'query');
    },
    PutTwoAccounts(variables: PutTwoAccountsMutationVariables, requestHeaders?: Dom.RequestInit["headers"]): Promise<{ data?: PutTwoAccountsMutation | undefined; extensions?: any; headers: Dom.Headers; status: number; errors?: GraphQLError[] | undefined; }> {
        return withWrapper((wrappedRequestHeaders) => client.rawRequest<PutTwoAccountsMutation>(PutTwoAccountsDocumentString, variables, {...requestHeaders, ...wrappedRequestHeaders}), 'PutTwoAccounts', 'mutation');
    }
  };
}
export type Sdk = ReturnType<typeof getSdk>;