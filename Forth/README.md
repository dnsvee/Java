# Forth interpreter

Attempt to create a Forth interpreter in Java. Some things work. Lots of functionality still missing.

Forth is a programming language with very little syntax and simple semantics. Where other languages have functions or methods Forth has words. Parameters are passed to words and values are returned from words by placing them on a globally available stack. Words (right now) don't have a local enviroment for local variables.

A Forth command looks like this:
1 2 +

This first puts the two numbers on the stack and then it runs the '+' command; it will pop the values from the stack, add them together and then it will place the return value on the stack. 

A stack output can be displayed with 'puts'. The 'nl' command displays a newline.

So '1 2 + puts nl' will output 3 and a newline

Comparing numbers is done with the the commands '< > <= >= ==':
3 2 >
5 6 <

This will cosume both numbers and place a boolean on the stack. 

Every object can be compared with every other object according Java's in 'equals' semantics.

## Strings

If Forth does not recognize a word it will consider it a string that is put on the stack as is. Prefacing a word with " will treat the word as a string. Strings can have embedded spaces in them prefaced with a backslash and it supports the \s and \n escape sequence.

These are all valid strings

Hello 
" Hello 
Hello\ world!
Line\sone\nLine\stwo\n

## Stack ordering

Objects on the stack can be rearranged if necessary. Here is an overview:

'dup'  create's a copy of the TOS 
'swap' swaps the two to objects with each other
'drop' drops the top object
'nip'  drops the object below the TOS
'over' duplicates the object under the TOS

There is also a second stack with the operators:

'twirl' swaps top of first and second stack
'raise' moves from main to second stack
'lower' moves from second stack to main

## Lambdas

A lambda groups statement in a single block and you can call the block using 'call':

[ something ] call

Lambdas on the stack are represented with numbers and placed by default on the second stack.

## Conditional

The if/then/else construct found in other languages is implemented as follows. 

boolean [ called when true ] [ called when false ] test 

'test' consumes the boolean on the top of the stack and if it is true it will call the first lambda otherwise it will call the second one.

## Looping

The while or for loop construct found in other languages is implemented as follows:

[ condition ] [ loop body ] while

The 'while' word fill first call the 'condition' lambda and it expects a boolean to be the TOS. If it is true it will call the second lambda. When the boolean is false if will end looping and pop the lambdas from the second stack/

## Constants

true, false, e, PI

## Lists

A list, implemented using ArrayDeque, is constructed like this:

( elements... ) 

Elements are added to the front like this:
<element> <list> push

Elements are removed from the front like this; the element is put on the stack:
<list> pop

The size of the list is queried like this:
<list> size

## Maps

A map is a dictionary that maps keys to values. A map is created and like this:
map!

A key/value pair is added like this
<value> <key> <map> put

The value associated to the map is retrieved using key like this:
<key> <map> get

The size of the map is queried like this:
<map> size
















