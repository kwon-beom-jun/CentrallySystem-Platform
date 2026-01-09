import JSEncrypt from 'jsencrypt';

/**
 * RSA 암호화 유틸리티
 * - 공개키로 비밀번호 암호화
 */
export class RsaEncryptionUtil {
  /**
   * RSA 공개키로 비밀번호 암호화
   * @param {string} password - 암호화할 비밀번호
   * @param {string} publicKey - Base64 형식의 RSA 공개키
   * @returns {string} - Base64 형식의 암호화된 비밀번호
   */
  static encryptPassword(password, publicKey) {
    if (!password || !publicKey) {
      throw new Error('비밀번호와 공개키가 필요합니다.');
    }

    const encrypt = new JSEncrypt();
    encrypt.setPublicKey(publicKey);
    
    const encrypted = encrypt.encrypt(password);
    
    if (!encrypted) {
      throw new Error('비밀번호 암호화에 실패했습니다.');
    }
    
    return encrypted;
  }
}

