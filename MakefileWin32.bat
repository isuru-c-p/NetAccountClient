javac -g:none NetLogin.java
jar cf JNetLogin.jar *.class *.gif *.png nz joptsimple
del -r *.class
echo  "\nUsage: java -cp JNetLogin.jar NetLogin\n"