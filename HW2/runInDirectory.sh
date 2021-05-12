string=""
for file in "$1"/*.java
do
    string="$string $file"
done

java Main $string
