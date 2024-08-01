import {base64Encode} from "./base64Encode";

/*
    Characters outside the Latin1 range (ISO-8859-1) include those that are part of Unicode but not represented within the Latin1 character set. This includes a wide range of characters such as:
        - Emojis: :grinning:, :smiley:, :smile:
        - Non-Latin alphabets: Greek (α, β, γ), Cyrillic (д, е, ж), etc.
        - Special symbols: Mathematical symbols (≠, ≥, ≤), currency symbols other than the dollar sign and euro (¥, ₹, ₽)
        - Accented characters from extended Latin alphabets not covered by Latin1: ā, ę, ī, ō, ū (Latin Extended-A for instance)
        - Characters from Asian scripts: Chinese (汉字), Japanese (かな), Korean (한글)
        - Special punctuation and other symbols not included in Latin1.
    Latin1 covers characters from the basic Latin alphabet, digits, punctuation marks, and some special characters like accented characters used in Western European languages, but it doesn't cover the vast array of characters available in Unicode.
*/
it("should encode json string containing Latin1 characters", () => {
    const expectedResult = "eyJuYW1lIjoiam9obiIsImh0bWwiOiLmsYnlrZcifQ=="
    const jsonResult = {
        "name": "john",
        "html": "汉字"
    }
    expect(base64Encode(jsonResult)).toBe(expectedResult)
})

/*
 This test demonstrates that without prior escaping, the `btoa` function cannot successfully base64 encode a JSON string
 if it includes characters beyond the Latin1 range. It ensures that the encoded output is appropriately prepared for inclusion
 in URL query strings or paths, which is the primary function of the `base64Encode` method.
*/
it("should fail to encode when json string contains characters outside Latin1 range", () => {
    const jsonResult = {
        "name": "john",
        "html": "汉字"
    }

    try {
        btoa(JSON.stringify(jsonResult))
        fail('Intentionally failing this test as encoding should fail and never reach here');
    } catch(e) {
        expect(e.message).toBe("Invalid character")
    }

})
