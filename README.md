# Elpremi
Very vanilla compression format implemented in Java on Maven.

Elpremi (meaning 'to squeeze' in [Esperanto](https://en.wikipedia.org/wiki/Esperanto)) is a compression format designed for no particular reason and completely unoptimised. It uses [my Huffman compression library](https://github.com/lambdacasserole/huff) and basically serializes the prefix code table along with the compressed data. This means that the `byte[]` you get from calling `compress()` contains all the information you need to call `decompress()` and get the original data back. For this reason, I've called Elpremi a 'compression format' but to be honest that's tooting my own bugle.

For the most part, Elpremi is just here to ship as the default Huffman-based compression format for when I resurrect [Denobo](https://github.com/lambdacasserole/denobo), the multi-agent middleware I designed with some classmates [Lee Oliver](https://github.com/Odyssic) and [Alex Mullen](https://github.com/AlexMullen).

## Installation
You can pull this package into your Maven project straight from here using JitPack. Add JitPack as a repository first:

```
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

Then add a dependency on Elpremi:

```
<dependencies>
    <dependency>
        <groupId>com.github.lambdacasserole</groupId>
        <artifactId>elpremi</artifactId>
        <version>v1.0</version>
    </dependency>
</dependencies>
```

## Limitations
Using Elpremi in production is discouraged. Compression ratios will look similar to [those for Huff](https://github.com/lambdacasserole/huff) but slightly worse because there's a great big serialized code table in there as well. This should be enough to tell you that there are options out there that are orders of magnitude better at compressing data and faster to boot.

## Contributing
For most intents and purposes, Elpremi is considered to fulfil its original use case. Bug fixes and suggestions are welcome, however, from any member of the community.
