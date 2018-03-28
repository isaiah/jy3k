a=$(jps | grep jython | awk '{print $1}')
jstack -l $a
