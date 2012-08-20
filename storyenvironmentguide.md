# Isla story environment guide

## Overview

This document covers the tools available to a programmer writing a story in Isla code.  This includes the extra data types related to the story world, like `room`, and the automatically generated variables, like `me`.

This guide is written for experienced programmers who wish to play around with Isla.  It is not intended for Isla's eventual target audience, young children.

## Types

### Pre-defined types

In the Isla language, a type is like a class that has only attributes.  You can create an instance of a type and assign it to a variable:

    palace is a room

This creates an object of type `room` that has some pre-defined attributes like `summary` and `items` and assigns this object to the variable `palace`.  You can assign a value to an attribute:

    palace summary is 'The floor is of marble.'

The pre-defined types that are accessible in the Isla story environment, and the attributes that are meaningful in the story environment:

* `room`
  * `summary` [string] A description of the room.  This will be displayed when the player enters the room, or when they type `look`.
  * `items` [list] A list of items in the room.  These are the items that the player can interact with when they are in the room.  In the future, there will be built-in item types.
  * `exit` [room] A room that this room connects to.  Because a room only has one exit attribute, rooms are connected in chains.  So, to connect a chain leading from the palace to the garden to the road, you would write: `palace exit is garden` and `garden exit is road`.

### Programmer-definable types

The Isla language supports the definition and instantiation of types by the programmer.  See `languageguide.md` for more details.

## Pre-defined variables

Some variables are pre-defined for the programmer.  These variables can be manipulated when defining a story.

* `my` The variable defining the player object.
  * `name` [string] The player's name.  Not currently used.
  * `summary` [string] A description of the player.  Displayed when the player types, `look at myself`.
  * `room` [room] The room the player is currently in.  This must be set in the Isla code to indicate which room the player starts in.
  * `items` [list] The items the player is carrying.

## Programming a story in the Isla language

When you play your story, all variables pre-defined in the environment and all objects  made from pre-defined types will be incorporated automatically into the story.  Following is an outline of programming a story in the Isla language.

### Set the attributes of the player

    my name is 'Mary'
    my summary is 'You are a young boy. You have no shoes on.'

The summary will be displayed if the player types `look at myself`.

### Create the rooms in which the story will take place

    hallway is a room
    hallway summary is 'It is dark. You hear a girl crying.'

    garden is a room
    garden summary is 'You see a girl crying.'
    hallway exit is garden

The summary will be displayed if the player types `look`.  The player can move to another connected room by typing something like `go into garden`.

### Set the room the player starts in

    my room is hallway

### Play your story
