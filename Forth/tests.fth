# this file containts test cases for the forth interpreter
# call this file with the command:
# java ForthTest -t tests.fth
# each line is considered a test case and will be processed individually 
# the interpreter will check if all the elements on the stack are 'true' values
# if not it considers the line to be a failing test case and output it to stdout

# test cases for variables
42 var num
num 42 ==
99 ! num num 99 ==

# test cases for strings
hello hello == 
hello world !=
hello strlen 5 ==
set! str! [] ==

# test cases for boolean operators
true false or
true true and
false not

# test cases for lists
list! var l
l str! [] ==
3 l apush 
l asize 1 ==
l apop 3 ==
l asize 0 ==
( 1 2 3 4 5 ) l aconcat ! l
3 l aget 4 ==
l asize 5 ==
l aclear asize 0 ==

# test cases for map
map! var m
m str! {} ==
three 3 m mput
four 4 m mput
m msize 2 ==
3 m mget three ==
3 m mdel
3 m mget null ==
m msize 1 ==

# test cases for sets
set! var s
1 s sadd
2 s sadd
3 s sadd
3 s shas
4 s shas not
set! var s2
2 s2 sadd
3 s2 sadd
4 s2 sadd
5 s2 sadd
s s2 sunion ssize 5 ==

# test cases for pairs
life 42 , var p
p str! (life,\ 42.0) ==
p ** 42 == swap life ==
1 2 , var p1
1 3 , var p2
2 1 , var p3
1 2 , var p4

p1 p2 < 
p3 p1 >
p1 p4 ==

# test cases for stack ops
false true nip
true false drop
false false over drop drop drop
false dup drop drop 

true false toss
grab not
false true toss flip 

# test cases for if statement
true if 1 else 2 then 1 == 
false if 1 else 2 then 2 == 

# test cases for regex
aaabbb a*b* matches
a a* matches
bbbb (b*) matches swap bbbb ==

# test cases for while loop
0 var cnt
false true true do while cnt 1 + ! cnt repeat cnt 2 ==

# test cases for math operatos
1 1 + 2 == 
2 3 * 6 ==
4 2 / 2 ==
5 3 - 2 ==
1 2 !=
1 1 ==

-1 abs 1 ==
1.4 floor 1 ==

1 2 < true
2 1 >
3 3 <=
4 4 >=

2 3 max 3 ==
2 3 min 2 ==
2 3 pow 8 ==
