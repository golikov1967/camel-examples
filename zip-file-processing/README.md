Camel Zip File Processing Example
=================================

To build this project use

    mvn clean install

To run this project with Maven use

    mvn clean camel:run

You will see some logs showing the content of the files in the zip file & also
see the unzipped files in the `target/unzipped` directory. Files will only be
processed once.

You will need to Ctrl-C the process to quit & you can run `mvn clean camel:run`
as many times as you like.

For more help see the Apache Camel documentation

  http://camel.apache.org/

