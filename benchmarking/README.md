# SetUp

Generate project template using : 
mvn archetype:generate -DinteractiveMode=false -DarchetypeGroupId=org.openjdk.jmh -DarchetypeArtifactId=jmh-java-benchmark-archetype -DgroupId=com.phonepe.growth.mustang -DartifactId=benchmarking -Dversion=1.0.0-SNAPSHOT


# Custom Logic

Add `@Benchmark` for such functions that need to be benchmarked.


# Build & execute

Build the maven project. And run benchmarks.jar artefact.

java -jar benchmarks.jar


## Execute select benchmarks

java -jar benchmarks.jar MustangSearchBenchmark


# Profiling

Attach agent of a profiler of your choice and initiate the execution

## For async-profiler 

java -agentpath:/Users/mohammed.irfanulla/async-profiler-2.6-macos/build/libasyncProfiler.so=start,event=cpu,file=profile.html -jar benchmarks.jar

## For YourKit

java -agentpath:/Applications/YourKit-Java-Profiler-2021.11.app/Contents/Resources/bin/mac/libyjpagent.dylib=delay=10000,listen=all -jar benchmarks.jar


