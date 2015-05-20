# Graph-generator
This data generator is used to generate a set of graphs. 
The graph database is composed of a set of positive graphs and a set of negative graphs.
This code first generates a set of patterns. Then these patterns are approximately embedded in positive graphs and negative graphs. These patterns appear more frequently in the posive set to guarantee that they are discriminative.

There are four main parameters, the number of graphs in the positive/negative set, the size of graph, the number of embedded patterns, and the size of embedded patterns.
