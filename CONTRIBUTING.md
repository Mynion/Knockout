# 🤝 Contributing to Knockout

First, thank you for your interest in contributing to **Knockout**.

Before making contributions, please read the following guide on building, testing, and submitting changes to the project.

---

## 💡 Workflow

1. 📬 **Contact First** – Please [open an issue](https://github.com/Mynion/Knockout/issues) or message me **before adding new features**.
2. 🧪 **Test Your Changes** – Use a local Spigot server to test functionality and ensure nothing breaks.
3. 🔁 **Open a Pull Request** – Describe clearly what your contribution does.
4. 📚 **Update Documentation** – If your PR introduces new features or config values, update `README.md` and the config section if needed.

---

## 🔁 Contributing

Follow this workflow:

1. **Fork** the repository using the "Fork" button on GitHub.
2. **Clone** your fork:
   ```bash
   git clone https://github.com/your-username/knockout.git
   cd knockout
   ```
3. **Add this repository as upstream** (to stay up to date):
   ```bash
   git remote add upstream https://github.com/Mynion/knockout.git
   git fetch upstream
   git checkout master
   git merge upstream/master
   ```

---

## 🧵 Branching Guidelines

Never work directly on `master`. Always create a separate branch for your changes:

1. **Create a new branch**:
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. Use clear branch names:
   - `feature/...` for new features
   - `fix/...` for bug fixes
   - `refactor/...` for code cleanup

3. **Push** your branch:
   ```bash
   git push origin feature/your-feature-name
   ```

4. Open a **Pull Request** to the `master` branch of the original repository.

> ✅ Make sure your changes are tested and well documented.

---

## 🛠️ Building the Plugin

This plugin is a **multi-module Maven project**, supporting multiple Minecraft versions.

You can build:

### 🔄 All versions (full plugin)

This will compile and package all supported version modules into a single `.jar`.

```bash
mvn clean package
```

The resulting `.jar` will contain support for **all included Minecraft versions**.

### ⚡ One specific version (faster build)

You can speed up the build by packaging the plugin for a **single version only**.

1. Set the desired version in `build/pom.xml`:

```xml
<properties>
    <output.version>1.21.6</output.version>
</properties>
```

2. Run this Maven command:

```bash
mvn package -pl build -Pbuild-one-version -am
```

This will only build the modules required for the version specified in `output.version`.

---

### 📦 Output Location

To automatically place the final `.jar` in a custom server directory, define a profile in your `~/.m2/settings.xml` file like this:

```xml
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">

    <profiles>
        <profile>
            <id>local-build</id>
            <properties>
                <plugin.output.directory>C:\Example\ServerOnVersion${output.version}\plugins</plugin.output.directory>   <!--Set your output path here-->
            </properties>
        </profile>
    </profiles>

    <activeProfiles>
        <activeProfile>local-build</activeProfile>
    </activeProfiles>
</settings>
```

> This makes it easy to deploy builds directly to your server’s `plugins/` folder.

---

## ✅ Development Requirements

- Java 17 or higher
- Maven 3.8+
- Internet connection (for dependency downloads)
- Git + GitHub account

---

## 🧼 Code Style & Guidelines

- Follow standard Java code conventions.
- Avoid putting version-specific logic in shared modules.
- Use meaningful commit messages, e.g., `feat: add fire damage immunity for knocked-out players`.
- Keep pull requests focused – don’t mix unrelated changes.

---

## 📜 License

By contributing, you agree that your changes will be licensed under the [GNU General Public License v3.0](LICENSE).

---

Thanks for helping improve **Knockout**! 🎮
