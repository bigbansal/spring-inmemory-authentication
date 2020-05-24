# spring-inmemory-authentication

#bootcamp is for in-memory-authentication
mvn clean install
#following should work
curl -u gbansal:password http://localhost:8080/greeting
#following should fail
curl -u gbansal:paord http://localhost:8080/greeting


