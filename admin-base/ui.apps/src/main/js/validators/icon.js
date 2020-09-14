import {IconLib} from '../constants'

export function libValidator(lib) {
  return Object.values(IconLib).includes(lib)
}