# StringConcatenationBenchmark Results

### OnePlus 8 - Android 13 - 19.10.2024

| Duration | Allocations | Name                                                      |
|----------|-------------|-----------------------------------------------------------|
| 533 ns   | 3 allocs    | stringBufferAdvancedManualConcatenation                   |
| 470 ns   | 3 allocs    | stringBufferAdvancedManualWithCharConcatenation           |
| 47.2 ns  | 1 allocs    | stringBufferInit                                          |
| 422 ns   | 1 allocs    | stringBufferReusedManualConcatenation                     |
| 396 ns   | 1 allocs    | stringBufferReusedManualWithCharConcatenation             |
| 384 ns   | 1 allocs    | stringBufferReusedThreadLocalManualConcatenation          |
| 422 ns   | 1 allocs    | stringBufferReusedThreadLocalManualWithCharConcatenation  |
| 691 ns   | 5 allocs    | stringBufferSimpleManualConcatenation                     |
| 547 ns   | 5 allocs    | stringBufferSimpleManualWithCharConcatenation             |
| 564 ns   | 3 allocs    | stringBuilderAdvancedManualConcatenation                  |
| 409 ns   | 3 allocs    | stringBuilderAdvancedManualWithCharConcatenation          |
| 24.1 ns  | 2 allocs    | stringBuilderInit                                         |
| 347 ns   | 1 allocs    | stringBuilderReusedManualConcatenation                    |
| 327 ns   | 1 allocs    | stringBuilderReusedManualWithCharConcatenation            |
| 254 ns   | 1 allocs    | stringBuilderReusedThreadLocalManualConcatenation         |
| 303 ns   | 1 allocs    | stringBuilderReusedThreadLocalManualWithCharConcatenation |
| 574 ns   | 5 allocs    | stringBuilderSimpleManualConcatenation                    |
| 649 ns   | 5 allocs    | stringBuilderSimpleManualWithCharConcatenation            |
| 803 ns   | 5 allocs    | stringCharArrayConcatenation                              |
| 799 ns   | 4 allocs    | stringCharArrayWithCharConcatenation                      |
| 1999 ns  | 23 allocs   | stringFormatConcatenation                                 |
| 841 ns   | 6 allocs    | stringJoinArrayConcatenation                              |
| 821 ns   | 8 allocs    | stringJoinListConcatenation                               |
| 664 ns   | 5 allocs    | stringPlusConcatenation                                   |
| 639 ns   | 5 allocs    | stringPlusWithCharConcatenation                           |
| 2.3 ns   | 0 allocs    | stringSetToLocalVariable                                  |
| 654 ns   | 5 allocs    | stringTemplateDollarConcatenation                         |

### Google Pixel 7 - Android 14 - 21.10.2024

| Duration | Allocations | Name                                                      |
|----------|-------------|-----------------------------------------------------------|
| 310 ns   | 3 allocs    | stringBufferAdvancedManualConcatenation                   |
| 330 ns   | 3 allocs    | stringBufferAdvancedManualWithCharConcatenation           |
| 21.3 ns  | 2 allocs    | stringBufferInit                                          |
| 276 ns   | 1 allocs    | stringBufferReusedManualConcatenation                     |
| 264 ns   | 1 allocs    | stringBufferReusedManualWithCharConcatenation             |
| 278 ns   | 1 allocs    | stringBufferReusedThreadLocalManualConcatenation          |
| 275 ns   | 1 allocs    | stringBufferReusedThreadLocalManualWithCharConcatenation  |
| 407 ns   | 5 allocs    | stringBufferSimpleManualConcatenation                     |
| 392 ns   | 5 allocs    | stringBufferSimpleManualWithCharConcatenation             |
| 291 ns   | 3 allocs    | stringBuilderAdvancedManualConcatenation                  |
| 267 ns   | 3 allocs    | stringBuilderAdvancedManualWithCharConcatenation          |
| 26.4 ns  | 2 allocs    | stringBuilderInit                                         |
| 210 ns   | 1 allocs    | stringBuilderReusedManualConcatenation                    |
| 215 ns   | 1 allocs    | stringBuilderReusedManualWithCharConcatenation            |
| 234 ns   | 1 allocs    | stringBuilderReusedThreadLocalManualConcatenation         |
| 226 ns   | 1 allocs    | stringBuilderReusedThreadLocalManualWithCharConcatenation |
| 368 ns   | 5 allocs    | stringBuilderSimpleManualConcatenation                    |
| 344 ns   | 5 allocs    | stringBuilderSimpleManualWithCharConcatenation            |
| 541 ns   | 5 allocs    | stringCharArrayConcatenation                              |
| 514 ns   | 4 allocs    | stringCharArrayWithCharConcatenation                      |
| 1176 ns  | 22 allocs   | stringFormatConcatenation                                 |
| 479 ns   | 6 allocs    | stringJoinArrayConcatenation                              |
| 553 ns   | 8 allocs    | stringJoinListConcatenation                               |
| 351 ns   | 5 allocs    | stringPlusConcatenation                                   |
| 387 ns   | 5 allocs    | stringPlusWithCharConcatenation                           |
| 1.9 ns   | 0 allocs    | stringSetToLocalVariable                                  |
| 383 ns   | 5 allocs    | stringTemplateDollarConcatenation                         |
