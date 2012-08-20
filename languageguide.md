# Isla Language Guide

## Overview

The Isla project comprises three parts.

First, the core language.  The basic syntax for expressions, assignments, blocks, conditionals and invocation.  The built in library of functions, like `write`.  The built in object types, like lists, and value types, like integers and strings.

Second, the story environment.  This is the tools available to someone writing a story in Isla code.  For example: the extra data types related to the story world, like `player` and `room`, and the automatically generated variables, like `me`.

Third, the storytelling itself.  The object that handles taking input from the player, like `go into garden`, manipulates the story state and outputs the result to the player, like `You are in the garden.  You can see a door to the palace.`.

This guide will cover the first part.  For the other parts, see `storyenvironmentguide.md` and `storytellingguide.md`.  These two other documents are not written, yet.

## Basic units of the language

Tokens in the language are delimited by whitespace.  Whitespace means spaces, tabs and new lines.  All tokens, besides strings, are case insensitive.

Expressions in the language are delimited by new lines.

An Isla program can only have one block.

## Blocks

Regardless of the number of new lines between expressions, all expressions in the program will belong to one block.

## Scope and environment

An Isla program has a single, global scope.  All variables, types and functions are always in scope.

The environment consists of two things.

First, the return value of the previously executed expression.  If an expression does not return anything, the return value is `nil`.

Second, the current context.  This contains a map of all available object types.  It also contains a map of variables and functions that are in scope.  This second map amounts to every variable ever instantiated and all the built in functions.

## Reserved words

The following words are reserved: `is`, `a`, `list`, `add`, `remove`, `to`, `from`, `true`, `false`.

## Variables

Variables are denoted by single words that match this regex: `[A-Za-z]+`.

### Variable assignment

Simple (non-object) variables can be strings or integers references to other variables and are assigned, thus:

    age is 1
    name is 'Mary'
    friend is isla

### Object attribute assignment

See the Objects section.

## Values

### Integers

An integer literal should match this regex: `[1-9][0-9]*`.  Notice that an integer must start with a digit between 1 and 9.

Currently, arithmetic is not supported.  This will probably change.

### Strings

A string literal is delimited by a pair of single quotation marks `'` or a pair of double quotation marks `"`.  Contents must match this regex: `^('|\")[A-Za-z0-9 \.,\\']+\1$`.

No string operations are supported, besides assignment to a variable.

### Booleans

Not yet implemented.

### Objects

Objects can be of an existing type, like `list`, or defined by the programmer.  Objects have zero or more attributes that may be set.  Objects have no methods.

#### Instantiation of a built-in type

    isla is a person

#### Instantiation of a programmer-defined type

    zach is a giraffe

You can see that, in order to make a new type, `giraffe`, you simply name the type.

### Attribute assignment

Objects may have any attribute name assigned on them, regardless of whether they were made from built-in and programmer-defined types.

    isla is a person
    mary is a person
    isla age is 2
    isla sound is 'pop'
    isla auntie is mary
    isla sdfjhsdhjfshjejhsscf is 3

Notice how integers, strings and objects may be assigned to an object attribute.

### Lists

List literals are not supported.  Lists can contains any combination of integers, strings and objects.  Lists may contain other lists.


Lists are a little strange.  They can only contain one instance of a variable, but can contain many instances of two literals that are identical.  For example:

    basket is a list
    add "banana" to basket            => ["banana"]
    add "banana" to basket            => ["banana" "banana"]
    banana is a fruit
    add banana to basket              => ["banana" "banana" banana]
    add banana to basket              => ["banana" "banana" banana]
    age is 1
    add age to basket                 => ["banana" "banana" mary age]
    add age to basket                 => ["banana" "banana" mary age]

Think of this in terms of references.  If you put a banana object into a list twice, you have put in two place holders that lead back to the same place.  Thus, the banana object only appears in the list once.  Literals are not references; they are actual things.  Therefore, when you put a string "banana" in and another string "banana" in, these are two separate things, so they both appear in the list.

#### List operations

##### Instantiation

    items is a list

##### Addition

    items is a list
    add 'hi to items

##### Removal

    items is a list
    remove 'bye' from items

##### Membership

    items has 'hi'

Returns true or false.  Not yet implemented.

##### Access

Individual items in lists may not be accessed.

## Conditionals

I haven't decided how to do these yet.  However, probably only `if` statements will be allowed, and they will occupy the same line as their single conditional branch, like: `open door if player has key`.

## Functions

It is not possible to define functions.  However, it is possible to call built in functions.  Currently, functions may take a maximum of one parameter.  This may change.

A function invocation:

    write name

It is possible to pass object attributes as parameters:

    write mary age

### Built in functions

`write` This function prints out the passed argument to the standard output.  It also returns the argument.
