import ACCESS_ENUM from "@/access/accessEnum";

/**
 * 检查权限（判断对当前登录用户是否具有某个权限）
 * @param loginUser
 * @param needAccess
 */
const checkAccess = (
  //登录用户的信息
  loginUser: API.LoginUserVO,
  //当前页面需要的权限
  needAccess = ACCESS_ENUM.NOT_LOGIN
) => {
  const loginUserAccess = loginUser.userRole ?? ACCESS_ENUM.NOT_LOGIN;
  if (needAccess === ACCESS_ENUM.NOT_LOGIN) {
    return true;
  }
  if (needAccess === ACCESS_ENUM.USER) {
    if (loginUserAccess === ACCESS_ENUM.NOT_LOGIN) {
      return false;
    }
  }
  if (needAccess === ACCESS_ENUM.ADMIN) {
    if (loginUserAccess !== ACCESS_ENUM.ADMIN) {
      return false;
    }
  }
  return true;
};

export default checkAccess;
