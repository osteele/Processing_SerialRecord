# Developer Notes

## Recipes

### Build the library

Do *one of*:

- In Visual Studio Code, select the *Terminal > Run Build Task* menu item.
- In the terminal, enter `mvn clean package`.

### Publish a new release

Build the library, and publish the library and the proejct file. The updated
version will be picked up (within a day?) by the Processing aggregator.

Note: This does *not* update the project version. For that, perform the steps in
the "update the library version" recipe.

`./scripts/publish.sh`

### Update the library version

1. In `./pom.xml`, near the top of the file, update the number `<version/>`
   number.
2. In `./library.properties`, update the `prettyVersion` value to the same
   string as the version number in `./pom.xml`.
3. In `./library.properties`, increment the `version` value.

## References

- <https://github.com/processing/processing/wiki/Library-Guidelines>
