#!/usr/bin/env bash
echo "BEFORE!" > before.txt
# export an environment variable to make sure it is communicated to the after script.
export FROM_BEFORE="Hello!"