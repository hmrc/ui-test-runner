export function base64Encode(jsonStr: any): string {
  return btoa(unescape(encodeURIComponent(JSON.stringify(jsonStr))));
}
