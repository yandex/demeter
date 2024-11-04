# StackTraceBenchmark Results

### OnePlus 8 - Android 13 - 19.02.2024

| Duration | Allocations | Name                         |
|----------|-------------|------------------------------|
| 1.8 ns   | 0 allocs    | stackWalkerGetInstance       |
| 29192 ns | 174 allocs  | stackWalkerStackTraceToArray |
| 28797 ns | 179 allocs  | stackWalkerStackTraceToList  |
| 1.8 ns   | 0 allocs    | threadCurrentThread          |
| 22373 ns | 174 allocs  | threadStackTrace             |
| 3723 ns  | 2 allocs    | throwableFillInStackTrace    |
| 3140 ns  | 3 allocs    | throwableInit                |
| 23352 ns | 165 allocs  | throwableOnlyStackTrace      |
| 22570 ns | 169 allocs  | throwableStackTrace          |

### OnePlus 8 - Android 13 - 25.02.2024

| Duration | Allocations | Name                              |
|----------|-------------|-----------------------------------|
| 329 ns   | 3 allocs    | stackWalkerGetInstance            |
| 20297 ns | 63 allocs   | stackWalkerStackTraceCallerMethod |
| 30531 ns | 177 allocs  | stackWalkerStackTraceToArray      |
| 29844 ns | 182 allocs  | stackWalkerStackTraceToList       |
| 1.8 ns   | 0 allocs    | threadCurrentThread               |
| 22069 ns | 174 allocs  | threadStackTrace                  |
| 22025 ns | 175 allocs  | threadStackTraceCallerMethod      |
| 4115 ns  | 2 allocs    | throwableFillInStackTrace         |
| 3437 ns  | 3 allocs    | throwableInit                     |
| 23441 ns | 165 allocs  | throwableOnlyStackTrace           |
| 22026 ns | 169 allocs  | throwableStackTrace               |
| 21992 ns | 168 allocs  | throwableStackTraceCallerMethod   |

### OnePlus 8 - Android 13 - 19.10.2024

| Duration | Allocations | Name                              |
|----------|-------------|-----------------------------------|
| 332 ns   | 3 allocs    | stackWalkerGetInstance            |
| 9329 ns  | 37 allocs   | stackWalkerStackTraceCallerMethod |
| 28124 ns | 174 allocs  | stackWalkerStackTraceToArray      |
| 27975 ns | 175 allocs  | stackWalkerStackTraceToList       |
| 1.7 ns   | 0 allocs    | threadCurrentThread               |
| 24692 ns | 175 allocs  | threadStackTrace                  |
| 25304 ns | 175 allocs  | threadStackTraceCallerMethod      |
| 4560 ns  | 2 allocs    | throwableFillInStackTrace         |
| 3684 ns  | 3 allocs    | throwableInit                     |
| 24943 ns | 166 allocs  | throwableOnlyStackTrace           |
| 24394 ns | 169 allocs  | throwableStackTrace               |
| 24526 ns | 169 allocs  | throwableStackTraceCallerMethod   |

### Google Pixel 7 - Android 14 - 21.10.2024

| Duration | Allocations | Name                              |
|----------|-------------|-----------------------------------|
| 228 ns   | 3 allocs    | stackWalkerGetInstance            |
| 5199 ns  | 37 allocs   | stackWalkerStackTraceCallerMethod |
| 14435 ns | 174 allocs  | stackWalkerStackTraceToArray      |
| 14570 ns | 175 allocs  | stackWalkerStackTraceToList       |
| 1.8 ns   | 0 allocs    | threadCurrentThread               |
| 18997 ns | 174 allocs  | threadStackTrace                  |
| 18948 ns | 174 allocs  | threadStackTraceCallerMethod      |
| 2628 ns  | 2 allocs    | throwableFillInStackTrace         |
| 2646 ns  | 3 allocs    | throwableInit                     |
| 15781 ns | 166 allocs  | throwableOnlyStackTrace           |
| 18753 ns | 169 allocs  | throwableStackTrace               |
| 18683 ns | 169 allocs  | throwableStackTraceCallerMethod   |
