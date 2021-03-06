export function getErrorMessage(err: any): string {
  let httpErrorMsg: string = '';

  if (err.status === 0) {
    httpErrorMsg += '<strong>HTTP ERROR</strong> - Cannot reach backend application';
  } else {
    httpErrorMsg += err.status != null ? '<strong>HTTP ERROR ' + err.status + '</strong> - ' : '<strong>HTTP ERROR</strong> - ';
    httpErrorMsg += (err._body !== null && err._body.toString().length > 0) ? err._body : err.statusText;
  }

  return httpErrorMsg;
}
