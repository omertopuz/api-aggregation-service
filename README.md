# API Aggregation Service
## Motivation
Building an aggregation service for consolidating the interface with supplied external
APIs into a single endpoint that can handle multiple logical requests in one network request.

### High Level Design
Basically, I used a request queue memory to save requests. Every request has a unique 
<br><br>`traceId` (generated in runtime as a UUID)<br>
<br> and the response is followed via this `traceId`. These requests are processed via a scheduler. 
The scheduler hits the external APIs periodically and saves the result into a response map memory.
The requester fetches the result from that response map by using `traceId`.

#### Why/How to scale up/down
When scaling up the instances, there is no potential problems about loosing requests.
When running multiple instances and a scaling down event occurs, then there is a possibility that some requests (at most 4 per instance) might disappear.
To avoid this problem, a distributed cache mechanism such as a `Redis` server would be a good decision.
I abstracted the request queue `RequestQueue` and response map `ResponseMap` objects to allow different implementation.
There are 2 implementation per interface. First/Default one is `in-memory` and the second one is a `redis` implementation.
If you want to activate distributed environment then run the application with the option below
<br><br>
`mvn clean install -Dapp.cache=redis`


### Tech Stack
* Java17
* Spring Boot
* Maven 3
* Docker

## Running the application locally ##
### How do I get set up? ###

* Summary of set up: Assuming that docker has already been installed in your local machine,
  just clone the repository into a folder than run the command below

```` docker-compose up -d ````
simply hit the endpoint below
````
GET http://localhost:8081/aggregation?
pricing=NL,CN&track=109347263,123456891&shipments=109347263,123456891
````
