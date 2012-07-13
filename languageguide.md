# Isla Language Guide

## Overview

The Isla project comprises three parts.

First, the core language.  The basic syntax for expressions, assignments, blocks, conditionals and invocation.  The built in library of functions, like `write`.  The built in object types, like lists, and value types, like integers and strings.

Second, the storytelling environment.  The extra data types related to the story world, like `player` and `room`.  The automatically generated variables, like `me`.

Third, the story itself.  The object that handles taking input from the player, like `go into garden`, manipulates the story state and outputs the result, like `You are in the garden.  You can see a door to the palace.`, to the player.

This guide will cover the first part.  For the other parts, see `storytellingguide.md` and `storyguide.md`.  These two other documents are not written, yet.

## Basic units of the language

Tokens in the language are delimited by whitespace.  Whitespace means spaces, tabs and new lines.  All tokens, besides strings, are case insensitive.

Expressions in the language are delimited by new lines.

An Isla program can only have one block.  Regardless of the number of new lines between expressions, a program will comprise a single block of statements.

## Blocks

Regardless of the number of new lines between expressions, an Isla program consists of a single block.

## Scope and environment

An Isla program has a single, global scope.  All variables, types and functions are always in scope.

The environment consists of two things.

First, the return value of the previously executed expression.  If an expression does not return anything, the return value should be set to `nil`.

Second, the current context.  This contains a map of all available object types.  It contains a map of built in functions, and variables that have been assigned.

## Reserved words

The following words are reserved: `is`, `a`, `list`, `add`, `remove`, `true`, `false`.

## Variables

Variables are denoted by single words that match this regex: `[A-Za-z]+`.

### Variable assignment

Simple (non-object) variables can be strings or integers and are assigned, thus:

    age is 1
    name is 'Mary'

### Object attribute assignment

See the Objects section.

## Values

### Integers

An integer literal should match this regex: `[1-9][0-9]*`.  Notice that an integer must start with a digit between 1 and 9.

Currently, arithmetic is not supported.  This will probably change.

### Strings

A string literal is delimited by single quotation marks: `'`.  Contents must match this regex: `[A-Za-z0-9 \.,\\]+`.

No string operations are supported, besides assignment to a variable.

### Booleans

Not yet implemented.

### Objects

Objects must be of an existing type, like `list` or `monster`.  New object types may not be defined in an Isla program.  Objects have zero or more attributes that may be set.  Objects have no methods.

#### Instantiation

    isla is a person

### Attribute assignment

    isla is a person
    mary is a person
    isla age is 2
    isla sound is 'pop'
    isla auntie is mary

Notice how integers, strings and objects may be assigned to an object attribute.

### Lists

List literals are not supported.

Lists are really unordered sets, in that they do not allow multiple instances of items that are deemed equal.

Equality of integers corresponds to equal numerical value.

Equality of strings corresponds to identical sequences of characters (case sensitive).

Equality of objects corresponds to identical sets of key/value pairs.  For example:

Equal:

    {:b => 2} {:b => 2}
    {} {}

Not equal:

    {:a => 1} {:b => 1}
    {:a => 1} {:a => 2}
    {:a => 1, :b => 2} {:a => 1}

Lists can contains any combination of integers, strings and objects.  Lists may not contain other lists.

#### List operations

##### Instantiation

    items is a list

##### Addition

    items is a list
    items add 'hi'

##### Removal

    items is a list
    items remove 'bye'

Removal has not, yet, been implemented.

##### Membership

    items has 'hi'

Returns true or false.  Not yet implemented.

##### Access

Individual items in lists may not be accessed.

## Conditionals

I haven't decided how to do these yet.  However, probably only `if` statements will be allowed, and they will occupy the same line as their conditional branch, like: `open door if player has key`.

## Functions

It is not possible to define functions.  However, it is possible to call built in functions.  Currently, functions may take a maximum of one parameter.  This may change.

A function invocation:

    write name

It is possible to pass object attributes as parameters:

    write mary age

### Built in functions

`write` This function prints out the passed argument to the standard output.  It also returns the argument.
