javac -g:none NetLogin.java
jar cf JNetLogin.jar *.class *.gif nz joptsimple
del -r *.class
echo  "\nUsage: java -cp JNetLogin.jar NetLogin\n"