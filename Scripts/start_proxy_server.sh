USER='downloadMonkey'
API_TOKEN='550e774adec84fdc62&a504bbf4b6@9C5dc'

download () {
	# wget -nv --progress=bar -O plugins/$1 --auth-no-challenge --user=$USER --password=$API_TOKEN $2
	curl -o plugins/$1 -u $USER:$API_TOKEN $2
}

while true; do
	download Proxy.jar http://143.110.237.48:8080/job/Skyopoly/job/main/lastSuccessfulBuild/artifact/out/Proxy-1.0-SNAPSHOT.jar

	java -jar BungeeCord.jar
	echo "Waiting for 5 seconds before the next iteration..."
    sleep 5
done