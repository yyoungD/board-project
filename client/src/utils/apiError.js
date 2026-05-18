export function getErrorMessage(error, fallbackMessage) {
  const responseData = error.response?.data;

  return (
    responseData?.message ||
    responseData?.error ||
    responseData?.detail ||
    fallbackMessage ||
    error.message
  );
}

export function getApiLoadError(error, messages = {}) {
  const networkError = messages.network ?? {
    title: '서버에 연결할 수 없습니다.',
    message: 'API 서버가 실행 중인지 확인한 뒤 다시 시도해주세요.'
  };

  const notFoundError = messages.notFound ?? {
    title: '요청한 데이터를 찾을 수 없습니다.',
    message: '삭제되었거나 존재하지 않는 데이터입니다.'
  };

  const serverError = messages.server ?? {
    title: '데이터를 불러오지 못했습니다.',
    message: '서버에 문제가 발생했습니다. 잠시 후 다시 시도해주세요.'
  };

  const defaultError = messages.default ?? {
    title: '데이터를 불러오지 못했습니다.',
    message: getErrorMessage(error, '요청을 처리하는 중 문제가 발생했습니다.')
  };

  if (!error.response) {
    return networkError;
  }

  if (error.response.status === 404) {
    return notFoundError;
  }

  if (error.response.status >= 500) {
    return serverError;
  }

  return defaultError;
}
