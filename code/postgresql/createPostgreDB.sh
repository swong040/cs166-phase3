#! /bin/bash
echo "creating db named ... "$USER"_DB"
createdb -h localhost -p $PGPORT $USER"_DB"
pg_ctl status

echo "Copying csv files ... "
sleep 1
cp ../data/*.csv /tmp/$USER/myDB/data/.

echo "Initializing tables .. "
sleep 1
# psql -h localhost -p $PGPORT $USER"_DB" < ../sql/create.sql

# to run multiple .sql files
# cat file1.sql file2.sql | psql -h localhost -p $PGPORT $USER"_DB" -1 -f -

cat ../sql/create.sql ../sql/triggers.sql ../sql/create_index.sql | psql -h localhost -p $PGPORT $USER"_DB"
