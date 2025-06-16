#!/bin/bash
# Validar commits en CI/CD

echo "🔍 Validando conventional commits en rango..."

# Obtener commits desde la rama main
COMMITS=$(git rev-list origin/main..HEAD --oneline)

if [ -z "$COMMITS" ]; then
    echo "✅ No hay commits nuevos para validar"
    exit 0
fi

PATTERN="^[a-f0-9]+ (feat|fix|docs|style|refactor|test|chore|perf|ci|build)(\(.+\))?: .{1,72}$"
INVALID_COMMITS=()

while IFS= read -r commit; do
    if ! echo "$commit" | grep -qE "$PATTERN"; then
        INVALID_COMMITS+=("$commit")
    fi
done <<< "$COMMITS"

if [ ${#INVALID_COMMITS[@]} -gt 0 ]; then
    echo "❌ Commits con formato inválido encontrados:"
    printf '%s\n' "${INVALID_COMMITS[@]}"
    echo ""
    echo "💡 Por favor, usa Conventional Commits format:"
    echo "   <type>[scope]: <description>"
    exit 1
fi

echo "✅ Todos los commits siguen el formato Conventional Commits"
