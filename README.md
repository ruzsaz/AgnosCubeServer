Web service that listens to data requests (POST requests) from Agnos' ReportServer.
A request consist of a Cube, a BaseVector and a list of DrillVectors.
The CubeServer determines the answers for the drills in the requests,
applies the necessary post-calculations (like Kaplan-Meier estimation),
and answers the request by sending the requested data in the response-body.  