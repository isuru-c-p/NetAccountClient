SOURCE = NetLogin.java \
		NetLoginPreferences.java \
		NetLoginConnection.java \
		PingRespHandler.java \
		PingSender.java \
		ReadResult.java \
		PasswordChanger.java \
		SPP_Packet.java

all: NetLogin.jar

NetLogin.jar: $(SOURCE)
	javac -g:none $(SOURCE)
	jar cf NetLogin.jar *.class nz joptsimple
	rm *.class
	echo -e "\nUsage: java -cp NetLogin.jar NetLogin\n"

clean:
	rm NetLogin.jar
