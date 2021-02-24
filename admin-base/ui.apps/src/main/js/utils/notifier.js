class Notifier {

  constructor($app) {
    this.$app = $app
  }

  filesNotUploaded(fileList) {
    return this.$app.notifyUser(
        'Info',
        `
        Some assets were not uploaded. Asset exists in this location:
        ${fileList.map((file) => file.name).join(', ')}
        `
    )
  }
}

export default Notifier