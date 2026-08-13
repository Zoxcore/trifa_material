#!/usr/bin/env bash
#
# check_nonascii.sh
#
# Report non-ASCII characters/bytes that are not in an allowed hex list.
#
# Default mode:
#   - Assumes input is UTF-8.
#   - Allowed hex values are Unicode code points, e.g. 00E9, 0xE9, U+20AC.
#   - Position is character position in the line, starting at 1.
#
# Byte mode (-b):
#   - Treats input as raw bytes.
#   - Allowed hex values are byte values 00-FF.
#   - Position is byte position in the line, starting at 1.
#
# Requires Bash 4+ for associative arrays.

usage() {
    cat <<'EOF'
Usage: check_nonascii.sh [-a allowed_hex_list] [-b] [-h] file [file...]

Checks one or more files for non-ASCII characters.

Options:
  -a allowed_hex_list  Allowed non-ASCII values in hex.
                       Values may be separated by spaces, commas, colons,
                       or semicolons.

                       Default mode: Unicode code points.
                       Examples: 00E9, 0xE9, U+00A9, 20AC

  -b                   Byte mode. Treat the file as raw bytes and allowed
                       values as byte values 00-FF.

  -h                   Show this help.

Output:
  filename:line:position: char='X' hex=YYYY

Exit status:
  0  no disallowed non-ASCII found
  1  disallowed non-ASCII found
  2  usage or file error
EOF
}

allow_arg=''
byte_mode=0

while getopts ':a:hb' opt; do
    case $opt in
        a)
            allow_arg=$OPTARG
            ;;
        b)
            byte_mode=1
            ;;
        h)
            usage
            exit 0
            ;;
        :)
            printf 'Error: option -%s requires an argument.\n' "$OPTARG" >&2
            exit 2
            ;;
        \?)
            printf 'Error: unknown option -%s.\n' "$OPTARG" >&2
            usage >&2
            exit 2
            ;;
    esac
done

shift $((OPTIND - 1))

if (( $# < 1 )); then
    usage >&2
    exit 2
fi

# In normal character mode we need a UTF-8 locale so Bash sees multibyte
# characters correctly. If no UTF-8 locale is found, fall back to byte mode.
if (( byte_mode == 0 )); then
    utf8_locale=$(locale -a 2>/dev/null | grep -iE '^(C|en_US)\.(UTF-?8)$' | head -n1)

    if [[ -n $utf8_locale ]]; then
        export LC_ALL=$utf8_locale
    else
        echo 'Warning: no UTF-8 locale found; falling back to byte mode.' >&2
        byte_mode=1
    fi
fi

if (( byte_mode )); then
    export LC_ALL=C
fi

declare -A allowed=()

# Parse the allowed hex list.
# Accepts things like:
#   00E9
#   0xE9
#   U+00E9
#   20AC
#   00E9,00A9,20AC
#   "00E9 00A9 U+20AC"
if [[ -n $allow_arg ]]; then
    IFS=$' \t\n,;:' read -r -a tokens <<< "$allow_arg"

    for token in "${tokens[@]}"; do
        [[ -z $token ]] && continue

        t=$token

        # Strip common hex/unicode prefixes.
        t=${t#0x}
        t=${t#0X}
        t=${t#U+}
        t=${t#u+}

        if [[ ! $t =~ ^[0-9A-Fa-f]+$ ]]; then
            printf 'Error: allowed value %s is not hexadecimal.\n' "'$token'" >&2
            exit 2
        fi

        val=$((16#$t))

        if (( byte_mode && val > 255 )); then
            printf 'Warning: allowed value 0x%X is > 0xFF; ignored in byte mode.\n' "$val" >&2
            continue
        fi

        allowed[$val]=1
    done
fi

rc=0

for file in "$@"; do
    if [[ -d $file || ! -r $file ]]; then
        printf 'Error: cannot read file %s.\n' "'$file'" >&2
        rc=2
        continue
    fi

    lineno=0

    # Read lines without stripping leading/trailing whitespace.
    # The "|| [[ -n $line ]]" part also processes a final line without newline.
    while IFS= read -r line || [[ -n $line ]]; do
        lineno=$((lineno + 1))
        len=${#line}

        for ((idx = 0; idx < len; idx++)); do
            c=${line:idx:1}

            [[ -n $c ]] || continue

            # Get numeric code point / byte value.
            # Bash printf: printf '%d' "'x" gives numeric value of character x.
            code=0
            printf -v code '%d' "'$c" 2>/dev/null || code=0

            # Fallback for undecodable characters: try interpreting as a byte.
            if (( code == 0 )); then
                code=$(LC_ALL=C printf '%d' "'$c" 2>/dev/null) || code=0
                [[ $code =~ ^-?[0-9]+$ ]] || code=0
            fi

            # Some environments may return negative values for high bytes.
            if (( code < 0 )); then
                code=$(( code & 0xFF ))
            fi

            # ASCII is 0x00-0x7F. Anything above 0x7F is non-ASCII.
            if (( code > 127 )) && [[ -z ${allowed[$code]:-} ]]; then
                if (( byte_mode )); then
                    hex=$(printf '%02X' "$code")
                else
                    hex=$(printf '%04X' "$code")
                fi

                printf "%s:%d:%d: char='%s' hex=%s\n" \
                    "$file" "$lineno" "$((idx + 1))" "$c" "$hex"

                if (( rc == 0 )); then
                    rc=1
                fi
            fi
        done
    done < "$file"
done

exit $rc

