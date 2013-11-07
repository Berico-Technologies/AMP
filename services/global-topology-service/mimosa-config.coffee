exports.config =
  minMimosaVersion:"1.0.1"
  modules: ['lint'
    'require'
    'minify'
    'bower'
    'require-lint'
  ]

  watch:
    sourceDir: "mimosa/assets"
    compiledDir: "src/main/resources/assets"
    javascriptDir: "javascripts"
    exclude: [/[/\\](\.|~)[^/\\]+$/]
  template:
    outputFileName: "templates"
    handlebars:
      helperFiles:["app/template/handlebars-helpers"]
  copy:
    extensions: ["js","css","png","jpg",
      "jpeg","gif","html","eot",
      "svg","ttf","woff","otf",
      "yaml","kml","ico","htc",
      "htm", "gexf"]
