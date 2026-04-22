# URLSession — Migration Guide

## RC9 → RC10

### `WiretapConfig` → `WiretapHttpConfig`

Renamed for clarity. Config classes are now in protocol-specific subpackages:

```diff
- import dev.skymansandy.wiretap.domain.model.config.WiretapConfig
- import dev.skymansandy.wiretap.domain.model.config.HeaderAction
- import dev.skymansandy.wiretap.domain.model.config.LogRetention
+ import dev.skymansandy.wiretap.domain.model.config.http.WiretapHttpConfig
+ import dev.skymansandy.wiretap.domain.model.config.http.HeaderAction
+ import dev.skymansandy.wiretap.domain.model.config.http.LogRetention
```

The constructor DSL is unchanged — only the config type name changed:

```diff
- WiretapURLSession { // this: WiretapConfig
+ WiretapURLSession { // this: WiretapHttpConfig
      enabled = true
  }
```
