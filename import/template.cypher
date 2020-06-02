using periodic commit 1000
LOAD CSV WITH HEADERS FROM 'file:///${csvFile}' as line
with line where ${idColumn} IS NOT null

merge (n:${label}${additionalLabels} {${idProperty}:${idColumn}})
${setFields};

LOAD CSV WITH HEADERS FROM 'file:///${csvFile}' as line
with count(line) as lines

Match (n:${label})
with count(n) as nodes, lines

Merge (n:Reconcile {nodeLabel:'${label}', recordCount:lines, nodeCount:nodes, difference:(lines - nodes), csvFile:'${csvFile}'})
return n;

