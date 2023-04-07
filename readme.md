# README

Chess position/perft genertor using [scalachess](https://github.com/lichess-org/scalachess)

## Compile

This requires [scala-cli](https://scala-cli.virtuslab.org/install) in order to compile and run.

```sh
scala-cli compile .
```

## Examples

### gen command

- Generate 64 positions after 32 moves depth for crazyhouse and save to `crazyhouse.csv`

```sh
scala-cli run . -- gen --variant crazyhouse --moves 32 --positions 64 --output crazyhouse.csv
```

- To generate positions for all variants we can just omit the variant from command line.

```sh
scala-cli run . -- gen --moves 32 --positions 64 # the default output file is `positions.csv`
```

### perft command

This command will first generate positions (same logic as `gen` command) and then generate perft for those positons.

- generate perft with depth 2 for all variants with the default config

```sh
scala-cli run . -- perft --depth 2
```

- generate perft with depth 2 for all variants with the custom config

```sh
echo "10,10\n23,99" > myconfig.csv
scala-cli run . -- perft --depth 2 --config myconfig.csv
```

