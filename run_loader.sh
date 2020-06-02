#!/usr/bin/env bash
#
# Parameters:
# 1 - Username - Neo4j user
# 2 - Password - Password for the username above
# 3 - Neo4j install directory
# 4 - Name of loadorder file to use
#
# Example
# ./run_loader.sh neo4j test /var/lib/neo4j/bin
#
# Assumes that the current database is fresh (no data)
#
#  Location of cypher-shell and admin programs - this changes depending on how we install
# Local
## AWS Tarball install
#shell_cmd='/home/neo4j/neo4j_current/bin/cypher-shell'
# AWS YUM install
#shell_cmd='/bin/cypher-shell'

## Function used when params are wrong
usage() {
    echo ''
    echo 'Invalid parameters:'
    echo '  P1 - Username - Neo4j user'
    echo '  P2 - Password - Password for the username above'
    echo '  P3 - Neo4j binary installation location '
    echo '  P4 - Name of loadorder file to use'
    exit 1
}

countObjects() {
    ### Count the ojbects
    runCypher "MATCH (n) return COUNT(n) as Nodes"
    ### Count the relationships
    runCypher "MATCH ()-[r]->() return COUNT(r) as Relationships"
    
}

runCypher() {
    ### Count the ojbects
    count_cmd="${cypher_cmd} \"$1\" "
    echo "count_cmd" # =${count_cmd}"
    # Run the cypher
    eval "${count_cmd}"
    status=$?
    if test $status -ne 0
    then
        echo "The database count command - check stderr for error messages"
        exit $status
    fi

}

# Make sure we have at least 4 parameters
if [ -z "$4"   ]
then
	echo "Invalid parameter count $1 $2 $3 $4"
    usage
fi

# Copy params to variables for clarity
user=$1
pw=$2
binDir=$3
loadOrder=$4
# Executables we need
shell_cmd=$binDir'/cypher-shell'


# get a list of all the cypher files in the current directory
# reading from a file to keep the order correct.
readarray -t loader_files < $loadOrder

# Assumes that you have already cleared the entire db before loading data
match_clause="MATCH (n) "

# cmd to call cypher
cypher_cmd="${shell_cmd} --format auto -u ${user} -p '${pw}'" # -a 'bolt+routing://localhost:7687'"

# Define our queries
count_cypher="${match_clause} RETURN COUNT (n)"

# Get before count
countObjects


### Load the data 
for loader in "${loader_files[@]}"
do
	loader_cmd="cat ${loader}.cypher | ${cypher_cmd} "
	echo "Executing loader_cmd for ${loader}"
	eval "${loader_cmd}"
	# store exit status
	# if success should return 0 exit status
	status=$?
	if test $status -ne 0
	then
		echo "The load failed - check stderr for error messages"
		exit ${status}
	fi
done

# store exit status
# if success should return 0 exit status
status=$?
if test $status -ne 0
then
    echo "The load failed - check stderr for error messages"
    exit ${status}
fi

### Count them again
countObjects