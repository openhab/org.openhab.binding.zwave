git ls-tree -r --name-only HEAD | while read filename; do
    VARA="$(git log -1 --format="%aI" -- "$filename")"
    VARB="$(awk '/dbReference/ {print $2}' $filename)"
VARX="${VARB#*>}"
VARY="${VARX%%<*}"
echo SET first_approval_time=\""${VARA}"\" WHERE id="${VARY}"
done
