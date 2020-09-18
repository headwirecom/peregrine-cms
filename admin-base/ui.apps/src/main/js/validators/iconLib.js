import {IconLib} from '../constants'

function validator(val) {
  return Object.values(IconLib).includes(val)
}

export default validator