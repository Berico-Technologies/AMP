# mimosa-config.coffee
exports.config = {
  watch:
    sourceDir: "assets"
    compiledDir: "../src/main/resources/assets"
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
  growl:
    onStartup: false
    onSuccess:
      javascript: true
      css: true
      template: true
      copy: true
}